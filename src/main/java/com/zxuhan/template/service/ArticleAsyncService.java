package com.zxuhan.template.service;

import com.google.gson.reflect.TypeToken;
import com.zxuhan.template.agent.ArticleAgentOrchestrator;
import com.zxuhan.template.agent.config.AgentConfig;
import com.zxuhan.template.manager.SseEmitterManager;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.entity.Article;
import com.zxuhan.template.model.enums.ArticlePhaseEnum;
import com.zxuhan.template.model.enums.ArticleStatusEnum;
import com.zxuhan.template.model.enums.SseMessageTypeEnum;
import com.zxuhan.template.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Async article task service.
 *
 * Supports two execution modes:
 * 1. Multi-agent orchestration mode (enabled via article.agent.orchestrator.enabled=true)
 * 2. Default mode (article.agent.orchestrator.enabled=false or unset)
 */
@Service
@Slf4j
public class ArticleAsyncService {

    @Resource
    private ArticleAgentService articleAgentService;

    @Resource
    private ArticleAgentOrchestrator articleAgentOrchestrator;

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private ArticleService articleService;

    /**
     * Phase 1: Asynchronously generate title options
     *
     * @param taskId task ID
     * @param topic  topic
     * @param style  article style (optional)
     */
    @Async("articleExecutor")
    public void executePhase1(String taskId, String topic, String style) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("Phase 1 async task started, taskId={}, topic={}, style={}, useOrchestrator={}",
                taskId, topic, style, useOrchestrator);

        try {
            // Update status and phase
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);
            articleService.updatePhase(taskId, ArticlePhaseEnum.TITLE_GENERATING);

            // Create state object
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setTopic(topic);
            state.setStyle(style);

            // Execute phase 1: generate title options (choose mode based on config)
            if (useOrchestrator) {
                articleAgentOrchestrator.executePhase1_GenerateTitles(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase1_GenerateTitles(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            // Save title options to database
            articleService.saveTitleOptions(taskId, state.getTitleOptions());

            // Update phase to awaiting title selection
            articleService.updatePhase(taskId, ArticlePhaseEnum.TITLE_SELECTING);

            // Push title options generated event
            Map<String, Object> data = new HashMap<>();
            data.put("titleOptions", state.getTitleOptions());
            sendSseMessage(taskId, SseMessageTypeEnum.TITLES_GENERATED, data);

            log.info("Phase 1 async task complete, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Phase 1 async task failed, taskId={}", taskId, e);

            // Update status to failed
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            // Push error message
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            // Complete SSE connection
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * Phase 2: Asynchronously generate outline (called after user confirms title)
     *
     * @param taskId task ID
     */
    @Async("articleExecutor")
    public void executePhase2(String taskId) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("Phase 2 async task started, taskId={}, useOrchestrator={}", taskId, useOrchestrator);

        try {
            // Get article info
            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new RuntimeException("Article not found");
            }

            // Create state object
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setStyle(article.getStyle());
            state.setUserDescription(article.getUserDescription());

            // Set title
            ArticleState.TitleResult title = new ArticleState.TitleResult();
            title.setMainTitle(article.getMainTitle());
            title.setSubTitle(article.getSubTitle());
            state.setTitle(title);

            // Execute phase 2: generate outline (choose mode based on config)
            if (useOrchestrator) {
                articleAgentOrchestrator.executePhase2_GenerateOutline(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase2_GenerateOutline(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            // Save outline to database
            Article articleToUpdate = articleService.getByTaskId(taskId);
            articleToUpdate.setOutline(GsonUtils.toJson(state.getOutline().getSections()));
            articleService.updateById(articleToUpdate);

            // Update phase to outline editing
            articleService.updatePhase(taskId, ArticlePhaseEnum.OUTLINE_EDITING);

            // Push outline generated event
            Map<String, Object> data = new HashMap<>();
            data.put("outline", state.getOutline().getSections());
            sendSseMessage(taskId, SseMessageTypeEnum.OUTLINE_GENERATED, data);

            log.info("Phase 2 async task complete, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Phase 2 async task failed, taskId={}", taskId, e);

            // Update status to failed
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            // Push error message
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            // Complete SSE connection
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * Phase 3: Asynchronously generate content and images (called after user confirms outline)
     *
     * @param taskId task ID
     */
    @Async("articleExecutor")
    public void executePhase3(String taskId) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("Phase 3 async task started, taskId={}, useOrchestrator={}", taskId, useOrchestrator);

        try {
            // Get article info
            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new RuntimeException("Article not found");
            }

            // Create state object
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setStyle(article.getStyle());

            // Load allowed image methods from database
            List<String> enabledMethods = null;
            if (article.getEnabledImageMethods() != null) {
                enabledMethods = GsonUtils.fromJson(
                        article.getEnabledImageMethods(),
                        new TypeToken<List<String>>(){}
                );
            }
            state.setEnabledImageMethods(enabledMethods);

            // Set title
            ArticleState.TitleResult title = new ArticleState.TitleResult();
            title.setMainTitle(article.getMainTitle());
            title.setSubTitle(article.getSubTitle());
            state.setTitle(title);

            // Set outline
            List<ArticleState.OutlineSection> outlineSections = GsonUtils.fromJson(
                    article.getOutline(),
                    new TypeToken<List<ArticleState.OutlineSection>>(){}
            );
            ArticleState.OutlineResult outlineResult = new ArticleState.OutlineResult();
            outlineResult.setSections(outlineSections);
            state.setOutline(outlineResult);

            // Execute phase 3: generate content + images (choose mode based on config)
            // Orchestrator mode supports parallel image generation
            if (useOrchestrator) {
                articleAgentOrchestrator.executePhase3_GenerateContent(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase3_GenerateContent(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            // Save full article to database
            articleService.saveArticleContent(taskId, state);

            // Update status to completed
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.COMPLETED, null);

            // Push completion event
            sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId", taskId));

            // Complete SSE connection
            sseEmitterManager.complete(taskId);

            log.info("Phase 3 async task complete, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Phase 3 async task failed, taskId={}", taskId, e);

            // Update status to failed
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            // Push error message
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            // Complete SSE connection
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * Handle agent message and push to SSE
     */
    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        Map<String, Object> data = buildMessageData(message, state);
        if (data != null) {
            sseEmitterManager.send(taskId, GsonUtils.toJson(data));
        }
    }

    /**
     * Build message data map
     *
     * @param message raw message
     * @param state   article state
     * @return message data, or null if the message is unrecognized
     */
    private Map<String, Object> buildMessageData(String message, ArticleState state) {
        // Handle streaming messages (with colon separator)
        String streamingPrefix2 = SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix();
        String streamingPrefix3 = SseMessageTypeEnum.AGENT3_STREAMING.getStreamingPrefix();
        String imageCompletePrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();
        
        if (message.startsWith(streamingPrefix2)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT2_STREAMING, message.substring(streamingPrefix2.length()));
        }
        
        if (message.startsWith(streamingPrefix3)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT3_STREAMING, message.substring(streamingPrefix3.length()));
        }
        
        if (message.startsWith(imageCompletePrefix)) {
            String imageJson = message.substring(imageCompletePrefix.length());
            return buildImageCompleteData(imageJson);
        }
        
        // Handle completion messages (enum values)
        return buildCompleteMessageData(message, state);
    }

    /**
     * Build streaming output data
     */
    private Map<String, Object> buildStreamingData(SseMessageTypeEnum type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.put("content", content);
        return data;
    }

    /**
     * Build image complete data
     */
    private Map<String, Object> buildImageCompleteData(String imageJson) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
        data.put("image", GsonUtils.fromJson(imageJson, ArticleState.ImageResult.class));
        return data;
    }

    /**
     * Build completion message data
     */
    private Map<String, Object> buildCompleteMessageData(String message, ArticleState state) {
        Map<String, Object> data = new HashMap<>();

        // Match by enum value
        if (SseMessageTypeEnum.AGENT1_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            data.put("title", state.getTitle());
        } else if (SseMessageTypeEnum.AGENT2_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            data.put("outline", state.getOutline().getSections());
        } else if (SseMessageTypeEnum.AGENT3_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
        } else if (SseMessageTypeEnum.AGENT4_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
            data.put("imageRequirements", state.getImageRequirements());
        } else if (SseMessageTypeEnum.AGENT5_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
            data.put("images", state.getImages());
        } else if (SseMessageTypeEnum.MERGE_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            data.put("fullContent", state.getFullContent());
        } else {
            return null;
        }
        
        return data;
    }

    /**
     * Send SSE message
     */
    private void sendSseMessage(String taskId, SseMessageTypeEnum type, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.putAll(additionalData);
        sseEmitterManager.send(taskId, GsonUtils.toJson(data));
    }
}
