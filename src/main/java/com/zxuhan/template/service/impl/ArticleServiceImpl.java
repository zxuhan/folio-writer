package com.zxuhan.template.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zxuhan.template.exception.BusinessException;
import com.zxuhan.template.exception.ErrorCode;
import com.zxuhan.template.exception.ThrowUtils;
import com.zxuhan.template.mapper.ArticleMapper;
import com.zxuhan.template.model.dto.article.ArticleQueryRequest;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.entity.Article;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.enums.ArticlePhaseEnum;
import com.zxuhan.template.model.enums.ArticleStatusEnum;
import com.zxuhan.template.model.vo.ArticleVO;
import com.zxuhan.template.service.ArticleAgentService;
import com.zxuhan.template.service.ArticleService;
import com.zxuhan.template.service.QuotaService;
import com.zxuhan.template.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.zxuhan.template.model.enums.ImageMethodEnum;

import static com.zxuhan.template.constant.UserConstant.ADMIN_ROLE;
import static com.zxuhan.template.constant.UserConstant.VIP_ROLE;

/**
 * Article service implementation
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private QuotaService quotaService;

    @Resource
    private ArticleAgentService articleAgentService;

    @Override
    public String createArticleTask(String topic, String style, List<String> enabledImageMethods, User loginUser) {
        // Process image methods: set default non-VIP methods for regular users if none selected
        List<String> finalImageMethods = processImageMethods(enabledImageMethods, loginUser);

        // Validate image method permissions (regular users cannot use NANO_BANANA or SVG_DIAGRAM)
        validateImageMethods(finalImageMethods, loginUser);

        // Generate task ID
        String taskId = IdUtil.simpleUUID();

        // Create article record
        Article article = new Article();
        article.setTaskId(taskId);
        article.setUserId(loginUser.getId());
        article.setTopic(topic);
        article.setStyle(style);
        article.setEnabledImageMethods(finalImageMethods != null && !finalImageMethods.isEmpty() 
                ? GsonUtils.toJson(finalImageMethods) : null);
        article.setStatus(ArticleStatusEnum.PENDING.getValue());
        article.setPhase(ArticlePhaseEnum.PENDING.getValue());
        article.setCreateTime(LocalDateTime.now());

        this.save(article);

        log.info("Article task created, taskId={}, userId={}, style={}", taskId, loginUser.getId(), style);
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArticleTaskWithQuotaCheck(String topic, String style, List<String> enabledImageMethods, User loginUser) {
        // Deduct quota and create task in the same transaction; quota is rolled back if task creation fails
        quotaService.checkAndConsumeQuota(loginUser);
        return createArticleTask(topic, style, enabledImageMethods, loginUser);
    }

    @Override
    public Article getByTaskId(String taskId) {
        return this.getOne(
                QueryWrapper.create().eq("taskId", taskId)
        );
    }

    @Override
    public ArticleVO getArticleDetail(String taskId, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article not found");

        // Check permission: users can only view their own articles (admins exempt)
        checkArticlePermission(article, loginUser);

        return ArticleVO.objToVo(article);
    }

    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser) {
        long current = request.getPageNum();
        long size = request.getPageSize();

        // Build query conditions
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .orderBy("createTime", false);

        // Non-admins can only view their own articles
        if (!ADMIN_ROLE.equals(loginUser.getUserRole())) {
            queryWrapper.eq("userId", loginUser.getId());
        } else if (request.getUserId() != null) {
            queryWrapper.eq("userId", request.getUserId());
        }

        // Filter by status
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            queryWrapper.eq("status", request.getStatus());
        }

        // Paginate
        Page<Article> articlePage = this.page(new Page<>(current, size), queryWrapper);

        // Convert to VO
        return convertToVOPage(articlePage);
    }

    @Override
    public boolean deleteArticle(Long id, User loginUser) {
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR);

        // Check permission: users can only delete their own articles (admins exempt)
        checkArticlePermission(article, loginUser);

        // Logical delete
        return this.removeById(id);
    }

    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("Article record not found, taskId={}", taskId);
            return;
        }

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        this.updateById(article);

        log.info("Article status updated, taskId={}, status={}", taskId, status.getValue());
    }

    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("Article record not found, taskId={}", taskId);
            return;
        }

        article.setMainTitle(state.getTitle().getMainTitle());
        article.setSubTitle(state.getTitle().getSubTitle());
        article.setOutline(GsonUtils.toJson(state.getOutline().getSections()));
        article.setContent(state.getContent());
        article.setFullContent(state.getFullContent());
        
        // Save cover image URL (extract URL of image at position=1)
        if (state.getImages() != null && !state.getImages().isEmpty()) {
            ArticleState.ImageResult cover = state.getImages().stream()
                .filter(img -> img.getPosition() != null && img.getPosition() == 1)
                .findFirst()
                .orElse(null);
            if (cover != null && cover.getUrl() != null) {
                article.setCoverImage(cover.getUrl());
            }
        }
        article.setImages(GsonUtils.toJson(state.getImages()));
        article.setCompletedTime(LocalDateTime.now());

        this.updateById(article);
        log.info("Article saved, taskId={}", taskId);
    }

    /**
     * Validate article access permission
     *
     * @param article   article entity
     * @param loginUser current user
     */
    private void checkArticlePermission(Article article, User loginUser) {
        if (!article.getUserId().equals(loginUser.getId()) &&
                !ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    /**
     * Convert article page to VO page
     *
     * @param articlePage article page
     * @return VO page
     */
    private Page<ArticleVO> convertToVOPage(Page<Article> articlePage) {
        Page<ArticleVO> articleVOPage = new Page<>();
        articleVOPage.setPageNumber(articlePage.getPageNumber());
        articleVOPage.setPageSize(articlePage.getPageSize());
        articleVOPage.setTotalRow(articlePage.getTotalRow());

        List<ArticleVO> articleVOList = articlePage.getRecords().stream()
                .map(ArticleVO::objToVo)
                .collect(Collectors.toList());
        articleVOPage.setRecords(articleVOList);

        return articleVOPage;
    }

    @Override
    public void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article not found");

        // Check permission
        checkArticlePermission(article, loginUser);

        // Validate current phase (must be TITLE_SELECTING)
        ArticlePhaseEnum currentPhase = ArticlePhaseEnum.getByValue(article.getPhase());
        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.TITLE_SELECTING,
                ErrorCode.OPERATION_ERROR, "This operation is not allowed in the current phase");

        // Save user-selected title and supplementary description
        article.setMainTitle(mainTitle);
        article.setSubTitle(subTitle);
        article.setUserDescription(userDescription);
        article.setPhase(ArticlePhaseEnum.OUTLINE_GENERATING.getValue());

        this.updateById(article);
        log.info("User confirmed title, taskId={}, mainTitle={}", taskId, mainTitle);
    }

    @Override
    public void confirmOutline(String taskId, List<ArticleState.OutlineSection> outline, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article not found");

        // Check permission
        checkArticlePermission(article, loginUser);

        // Validate current phase (must be OUTLINE_EDITING)
        ArticlePhaseEnum currentPhase = ArticlePhaseEnum.getByValue(article.getPhase());
        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.OUTLINE_EDITING,
                ErrorCode.OPERATION_ERROR, "This operation is not allowed in the current phase");

        // Save user-edited outline
        article.setOutline(GsonUtils.toJson(outline));
        article.setPhase(ArticlePhaseEnum.CONTENT_GENERATING.getValue());

        this.updateById(article);
        log.info("User confirmed outline, taskId={}, sectionsCount={}", taskId, outline.size());
    }

    @Override
    public void updatePhase(String taskId, ArticlePhaseEnum phase) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("Article record not found, taskId={}", taskId);
            return;
        }

        article.setPhase(phase.getValue());
        this.updateById(article);
        log.info("Article phase updated, taskId={}, phase={}", taskId, phase.getValue());
    }

    @Override
    public void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("Article record not found, taskId={}", taskId);
            return;
        }

        article.setTitleOptions(GsonUtils.toJson(titleOptions));
        this.updateById(article);
        log.info("Title options saved, taskId={}, optionsCount={}", taskId, titleOptions.size());
    }

    @Override
    public List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article not found");

        // Check permission
        checkArticlePermission(article, loginUser);

        // Check VIP permission (regular users cannot use AI outline modification)
        ThrowUtils.throwIf(!isVipOrAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,
                "AI outline modification is available to VIP members only");

        // Validate current phase (must be OUTLINE_EDITING)
        ArticlePhaseEnum currentPhase = ArticlePhaseEnum.getByValue(article.getPhase());
        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.OUTLINE_EDITING,
                ErrorCode.OPERATION_ERROR, "This operation is not allowed in the current phase");

        // Get current outline
        List<ArticleState.OutlineSection> currentOutline = GsonUtils.fromJson(
                article.getOutline(),
                new TypeToken<List<ArticleState.OutlineSection>>(){}
        );

        // Call AI to modify outline
        List<ArticleState.OutlineSection> modifiedOutline = articleAgentService.aiModifyOutline(
                article.getMainTitle(),
                article.getSubTitle(),
                currentOutline,
                modifySuggestion
        );

        // Save modified outline
        article.setOutline(GsonUtils.toJson(modifiedOutline));
        this.updateById(article);

        log.info("AI outline modification complete, taskId={}, sectionsCount={}", taskId, modifiedOutline.size());
        return modifiedOutline;
    }

    /**
     * Process image methods.
     * If the user made no selection, set default non-VIP methods for regular users; VIP users have no restriction.
     */
    private List<String> processImageMethods(List<String> enabledImageMethods, User loginUser) {
        // If the user already selected methods, return as-is
        if (enabledImageMethods != null && !enabledImageMethods.isEmpty()) {
            return enabledImageMethods;
        }

        // VIP and admins: no restriction; null means all methods are supported
        if (isVipOrAdmin(loginUser)) {
            return null;
        }

        // Regular users: return default non-VIP methods
        return List.of(
                ImageMethodEnum.PEXELS.getValue(),
                ImageMethodEnum.MERMAID.getValue(),
                ImageMethodEnum.ICONIFY.getValue(),
                ImageMethodEnum.EMOJI_PACK.getValue()
        );
    }

    /**
     * Validate image method permissions.
     * Regular users cannot use NANO_BANANA or SVG_DIAGRAM.
     */
    private void validateImageMethods(List<String> enabledImageMethods, User loginUser) {
        if (enabledImageMethods == null || enabledImageMethods.isEmpty()) {
            return;
        }

        // VIP and admins: no restriction
        if (isVipOrAdmin(loginUser)) {
            return;
        }

        // Regular user restriction
        for (String method : enabledImageMethods) {
            if (ImageMethodEnum.NANO_BANANA.getValue().equals(method) ||
                ImageMethodEnum.SVG_DIAGRAM.getValue().equals(method)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,
                        "Advanced image features (AI generation, SVG diagrams) are available to VIP members only");
            }
        }
    }

    /**
     * Check whether the user is a VIP or admin
     */
    private boolean isVipOrAdmin(User user) {
        return ADMIN_ROLE.equals(user.getUserRole()) || 
               VIP_ROLE.equals(user.getUserRole());
    }
}
