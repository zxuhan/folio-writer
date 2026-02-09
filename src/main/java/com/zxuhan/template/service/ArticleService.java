package com.zxuhan.template.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zxuhan.template.model.dto.article.ArticleQueryRequest;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.entity.Article;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.enums.ArticlePhaseEnum;
import com.zxuhan.template.model.enums.ArticleStatusEnum;
import com.zxuhan.template.model.vo.ArticleVO;

import java.util.List;

/**
 * Article service interface
 */
public interface ArticleService extends IService<Article> {

    /**
     * Create an article task
     *
     * @param topic               topic
     * @param style               article style (optional)
     * @param enabledImageMethods allowed image methods (optional)
     * @param loginUser           current logged-in user
     * @return task ID
     */
    String createArticleTask(String topic, String style, List<String> enabledImageMethods, User loginUser);

    /**
     * Create an article task with quota check.
     * Quota deduction and task creation are performed in the same transaction to ensure atomicity.
     *
     * @param topic               topic
     * @param style               article style (optional)
     * @param enabledImageMethods allowed image methods (optional)
     * @param loginUser           current logged-in user
     * @return task ID
     */
    String createArticleTaskWithQuotaCheck(String topic, String style, List<String> enabledImageMethods, User loginUser);

    /**
     * Get article by task ID
     *
     * @param taskId task ID
     * @return article entity
     */
    Article getByTaskId(String taskId);

    /**
     * Get article detail (with permission check)
     *
     * @param taskId    task ID
     * @param loginUser current logged-in user
     * @return article VO
     */
    ArticleVO getArticleDetail(String taskId, User loginUser);

    /**
     * List articles by page
     *
     * @param request   query request
     * @param loginUser current logged-in user
     * @return paginated result
     */
    Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser);

    /**
     * Delete article (with permission check)
     *
     * @param id        article ID
     * @param loginUser current logged-in user
     * @return whether deletion succeeded
     */
    boolean deleteArticle(Long id, User loginUser);

    /**
     * Update article status
     *
     * @param taskId       task ID
     * @param status       status enum
     * @param errorMessage error message (optional)
     */
    void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage);

    /**
     * Save article content
     *
     * @param taskId task ID
     * @param state  article state object
     */
    void saveArticleContent(String taskId, ArticleState state);

    /**
     * Confirm title (after user selects)
     *
     * @param taskId          task ID
     * @param mainTitle       selected main title
     * @param subTitle        selected subtitle
     * @param userDescription user's supplementary description
     * @param loginUser       current logged-in user
     */
    void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser);

    /**
     * Confirm outline (after user edits)
     *
     * @param taskId    task ID
     * @param outline   user-edited outline
     * @param loginUser current logged-in user
     */
    void confirmOutline(String taskId, List<ArticleState.OutlineSection> outline, User loginUser);

    /**
     * Update phase
     *
     * @param taskId task ID
     * @param phase  phase enum
     */
    void updatePhase(String taskId, ArticlePhaseEnum phase);

    /**
     * Save title options
     *
     * @param taskId       task ID
     * @param titleOptions list of title options
     */
    void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions);

    /**
     * AI-assisted outline modification
     *
     * @param taskId           task ID
     * @param modifySuggestion user's modification suggestion
     * @param loginUser        current logged-in user
     * @return modified outline
     */
    List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser);
}
