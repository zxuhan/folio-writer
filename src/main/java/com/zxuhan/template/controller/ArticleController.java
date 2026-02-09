package com.zxuhan.template.controller;

import com.mybatisflex.core.paginate.Page;
import com.zxuhan.template.common.BaseResponse;
import com.zxuhan.template.common.DeleteRequest;
import com.zxuhan.template.common.ResultUtils;
import com.zxuhan.template.exception.ErrorCode;
import com.zxuhan.template.exception.ThrowUtils;
import com.zxuhan.template.manager.SseEmitterManager;
import com.zxuhan.template.model.dto.article.ArticleAiModifyOutlineRequest;
import com.zxuhan.template.model.dto.article.ArticleConfirmOutlineRequest;
import com.zxuhan.template.model.dto.article.ArticleConfirmTitleRequest;
import com.zxuhan.template.model.dto.article.ArticleCreateRequest;
import com.zxuhan.template.model.dto.article.ArticleQueryRequest;
import com.zxuhan.template.model.dto.article.ArticleState;

import java.util.List;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.enums.ArticleStyleEnum;
import com.zxuhan.template.model.vo.AgentExecutionStats;
import com.zxuhan.template.model.vo.ArticleVO;
import com.zxuhan.template.service.AgentLogService;
import com.zxuhan.template.service.ArticleAsyncService;
import com.zxuhan.template.service.ArticleService;
import com.zxuhan.template.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Article API
 *
 */
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleAsyncService articleAsyncService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private UserService userService;

    @Resource
    private AgentLogService agentLogService;

    /**
     * Create article task
     */
    @PostMapping("/create")
    @Operation(summary = "Create article task")
    public BaseResponse<String> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTopic() == null || request.getTopic().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Topic cannot be empty");
        // Validate style parameter (nullable)
        ThrowUtils.throwIf(!ArticleStyleEnum.isValid(request.getStyle()),
                ErrorCode.PARAMS_ERROR, "Invalid article style");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // Check and consume quota + create article task (within the same transaction)
        String taskId = articleService.createArticleTaskWithQuotaCheck(
                request.getTopic(),
                request.getStyle(),
                request.getEnabledImageMethods(),
                loginUser
        );

        // Execute phase 1 asynchronously: generate title options
        articleAsyncService.executePhase1(
                taskId,
                request.getTopic(),
                request.getStyle()
        );

        return ResultUtils.success(taskId);
    }

    /**
     * SSE progress stream
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "Get article generation progress (SSE)")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");

        // Validate permission (checks task existence and user access rights internally)
        User loginUser = userService.getLoginUser(httpServletRequest);
        articleService.getArticleDetail(taskId, loginUser);

        // Create SSE Emitter
        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);

        log.info("SSE connection established, taskId={}", taskId);
        return emitter;
    }

    /**
     * Get article details
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "Get article details")
    public BaseResponse<ArticleVO> getArticle(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);

        return ResultUtils.success(articleVO);
    }

    /**
     * List articles with pagination
     */
    @PostMapping("/list")
    @Operation(summary = "List articles with pagination")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest request,
                                                       HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(request, loginUser);

        return ResultUtils.success(articleVOPage);
    }

    /**
     * Delete article
     */
    @PostMapping("/delete")
    @Operation(summary = "Delete article")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }

    /**
     * Confirm title and provide supplementary description
     */
    @PostMapping("/confirm-title")
    @Operation(summary = "Confirm title and provide supplementary description")
    public BaseResponse<Void> confirmTitle(@RequestBody ArticleConfirmTitleRequest request,
                                            HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getSelectedMainTitle() == null || request.getSelectedMainTitle().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Main title cannot be empty");
        ThrowUtils.throwIf(request.getSelectedSubTitle() == null || request.getSelectedSubTitle().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Subtitle cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // Confirm title
        articleService.confirmTitle(
                request.getTaskId(),
                request.getSelectedMainTitle(),
                request.getSelectedSubTitle(),
                request.getUserDescription(),
                loginUser
        );

        // Execute phase 2 asynchronously: generate outline
        articleAsyncService.executePhase2(request.getTaskId());

        return ResultUtils.success(null);
    }

    /**
     * Confirm outline
     */
    @PostMapping("/confirm-outline")
    @Operation(summary = "Confirm outline")
    public BaseResponse<Void> confirmOutline(@RequestBody ArticleConfirmOutlineRequest request,
                                              HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getOutline() == null || request.getOutline().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Outline cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // Confirm outline
        articleService.confirmOutline(
                request.getTaskId(),
                request.getOutline(),
                loginUser
        );

        // Execute phase 3 asynchronously: generate content + images
        articleAsyncService.executePhase3(request.getTaskId());

        return ResultUtils.success(null);
    }

    /**
     * AI-assisted outline modification
     */
    @PostMapping("/ai-modify-outline")
    @Operation(summary = "AI-assisted outline modification")
    public BaseResponse<List<ArticleState.OutlineSection>> aiModifyOutline(
            @RequestBody ArticleAiModifyOutlineRequest request,
            HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getModifySuggestion() == null || request.getModifySuggestion().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Modification suggestion cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // AI modify outline
        List<ArticleState.OutlineSection> modifiedOutline = articleService.aiModifyOutline(
                request.getTaskId(),
                request.getModifySuggestion(),
                loginUser
        );

        return ResultUtils.success(modifiedOutline);
    }

    /**
     * Get task execution logs
     */
    @GetMapping("/execution-logs/{taskId}")
    @Operation(summary = "Get task execution logs")
    public BaseResponse<AgentExecutionStats> getExecutionLogs(@PathVariable String taskId) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");

        AgentExecutionStats stats = agentLogService.getExecutionStats(taskId);
        return ResultUtils.success(stats);
    }
}
