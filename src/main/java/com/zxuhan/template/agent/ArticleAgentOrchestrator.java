package com.zxuhan.template.agent;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.zxuhan.template.agent.agents.*;
import com.zxuhan.template.agent.config.AgentConfig;
import com.zxuhan.template.agent.context.StreamHandlerContext;
import com.zxuhan.template.agent.parallel.ParallelImageGenerator;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.enums.SseMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Article agent orchestrator.
 * Uses Spring AI Alibaba's StateGraph to orchestrate multiple agents.
 */
@Service
@Slf4j
public class ArticleAgentOrchestrator {

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private TitleGeneratorAgent titleGeneratorAgent;

    @Resource
    private OutlineGeneratorAgent outlineGeneratorAgent;

    @Resource
    private ContentGeneratorAgent contentGeneratorAgent;

    @Resource
    private ImageAnalyzerAgent imageAnalyzerAgent;

    @Resource
    private ParallelImageGenerator parallelImageGenerator;

    @Resource
    private ContentMergerAgent contentMergerAgent;

    // region State key constants

    private static final String KEY_TASK_ID = "taskId";
    private static final String KEY_TOPIC = "topic";
    private static final String KEY_STYLE = "style";
    private static final String KEY_USER_DESCRIPTION = "userDescription";
    private static final String KEY_MAIN_TITLE = "mainTitle";
    private static final String KEY_SUB_TITLE = "subTitle";
    private static final String KEY_TITLE_OPTIONS = "titleOptions";
    private static final String KEY_OUTLINE = "outline";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_CONTENT_WITH_PLACEHOLDERS = "contentWithPlaceholders";
    private static final String KEY_IMAGE_REQUIREMENTS = "imageRequirements";
    private static final String KEY_IMAGES = "images";
    private static final String KEY_FULL_CONTENT = "fullContent";
    private static final String KEY_ENABLED_IMAGE_METHODS = "enabledImageMethods";

    // endregion

    /**
     * Phase 1: Generate title options.
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase1_GenerateTitles(ArticleState state, Consumer<String> streamHandler) {
        log.info("Phase 1 (multi-agent orchestration): Start generating title options, taskId={}", state.getTaskId());

        try {
            // Build input state
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_TOPIC, state.getTopic());
            inputs.put(KEY_STYLE, state.getStyle());

            // Build and execute graph
            StateGraph graph = buildPhase1Graph();
            CompiledGraph compiledGraph = graph.compile();

            Optional<OverAllState> result = compiledGraph.invoke(inputs);

            if (result.isPresent()) {
                OverAllState finalState = result.get();

                @SuppressWarnings("unchecked")
                List<ArticleState.TitleOption> titleOptions = finalState.value(KEY_TITLE_OPTIONS)
                        .map(v -> (List<ArticleState.TitleOption>) v)
                        .orElse(null);

                if (titleOptions != null) {
                    state.setTitleOptions(titleOptions);
                    streamHandler.accept(SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
                    log.info("Phase 1 (multi-agent orchestration): Title options generated, count={}", titleOptions.size());
                } else {
                    throw new RuntimeException("Failed to generate title options: result is null");
                }
            } else {
                throw new RuntimeException("Failed to generate title options: execution result is empty");
            }

        } catch (Exception e) {
            log.error("Phase 1 (multi-agent orchestration): Title generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Failed to generate title options: " + e.getMessage(), e);
        }
    }

    /**
     * Phase 2: Generate outline.
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase2_GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        log.info("Phase 2 (multi-agent orchestration): Start generating outline, taskId={}", state.getTaskId());

        // Store streaming handler in ThreadLocal
        StreamHandlerContext.set(streamHandler);

        try {
            // Build input state
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_MAIN_TITLE, state.getTitle().getMainTitle());
            inputs.put(KEY_SUB_TITLE, state.getTitle().getSubTitle());
            inputs.put(KEY_USER_DESCRIPTION, state.getUserDescription());
            inputs.put(KEY_STYLE, state.getStyle());

            // Build and execute graph
            StateGraph graph = buildPhase2Graph();
            CompiledGraph compiledGraph = graph.compile();

            Optional<OverAllState> result = compiledGraph.invoke(inputs);

            if (result.isPresent()) {
                OverAllState finalState = result.get();

                ArticleState.OutlineResult outline = finalState.value(KEY_OUTLINE)
                        .map(v -> {
                            if (v instanceof ArticleState.OutlineResult) {
                                return (ArticleState.OutlineResult) v;
                            }
                            return null;
                        })
                        .orElse(null);

                if (outline != null) {
                    state.setOutline(outline);
                    streamHandler.accept(SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
                    log.info("Phase 2 (multi-agent orchestration): Outline generated, sections={}", outline.getSections().size());
                } else {
                    throw new RuntimeException("Failed to generate outline: result is null");
                }
            } else {
                throw new RuntimeException("Failed to generate outline: execution result is empty");
            }

        } catch (Exception e) {
            log.error("Phase 2 (multi-agent orchestration): Outline generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Failed to generate outline: " + e.getMessage(), e);
        } finally {
            // Clear ThreadLocal
            StreamHandlerContext.clear();
        }
    }

    /**
     * Phase 3: Generate content + images.
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase3_GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        log.info("Phase 3 (multi-agent orchestration): Start generating content + images, taskId={}", state.getTaskId());

        // Store streaming handler in ThreadLocal
        StreamHandlerContext.set(streamHandler);

        try {
            // Build input state (streamHandler excluded to avoid serialization issues)
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(KEY_TASK_ID, state.getTaskId());
            inputs.put(KEY_MAIN_TITLE, state.getTitle().getMainTitle());
            inputs.put(KEY_SUB_TITLE, state.getTitle().getSubTitle());
            inputs.put(KEY_OUTLINE, state.getOutline());
            inputs.put(KEY_STYLE, state.getStyle());
            inputs.put(KEY_ENABLED_IMAGE_METHODS, state.getEnabledImageMethods());

            // Build and execute graph
            StateGraph graph = buildPhase3Graph();
            CompiledGraph compiledGraph = graph.compile();

            Optional<OverAllState> result = compiledGraph.invoke(inputs);

            if (result.isPresent()) {
                OverAllState finalState = result.get();

                // Extract content with placeholders (preferred if present)
                String contentWithPlaceholders = finalState.value(KEY_CONTENT_WITH_PLACEHOLDERS)
                        .map(Object::toString)
                        .orElse(null);

                // Extract raw content (fallback)
                String content = finalState.value(KEY_CONTENT)
                        .map(Object::toString)
                        .orElse(null);

                // Extract image requirements
                @SuppressWarnings("unchecked")
                List<ArticleState.ImageRequirement> imageRequirements = finalState.value(KEY_IMAGE_REQUIREMENTS)
                        .map(v -> (List<ArticleState.ImageRequirement>) v)
                        .orElse(null);

                // Extract image results
                @SuppressWarnings("unchecked")
                List<ArticleState.ImageResult> images = finalState.value(KEY_IMAGES)
                        .map(v -> (List<ArticleState.ImageResult>) v)
                        .orElse(null);

                // Extract full content
                String fullContent = finalState.value(KEY_FULL_CONTENT)
                        .map(Object::toString)
                        .orElse(null);

                // Update state (prefer content with placeholders)
                if (contentWithPlaceholders != null) {
                    state.setContent(contentWithPlaceholders);
                } else if (content != null) {
                    state.setContent(content);
                }
                streamHandler.accept(SseMessageTypeEnum.AGENT3_COMPLETE.getValue());

                if (imageRequirements != null) {
                    state.setImageRequirements(imageRequirements);
                    streamHandler.accept(SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
                }

                if (images != null) {
                    state.setImages(images);
                    streamHandler.accept(SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
                }

                if (fullContent != null) {
                    state.setFullContent(fullContent);
                    streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());
                }

                log.info("Phase 3 (multi-agent orchestration): Content + images generated, contentLength={}, imageCount={}",
                        contentWithPlaceholders != null ? contentWithPlaceholders.length() : (content != null ? content.length() : 0),
                        images != null ? images.size() : 0);

            } else {
                throw new RuntimeException("Failed to generate content + images: execution result is empty");
            }

        } catch (Exception e) {
            log.error("Phase 3 (multi-agent orchestration): Content + image generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Failed to generate content + images: " + e.getMessage(), e);
        } finally {
            // Clear ThreadLocal
            StreamHandlerContext.clear();
        }
    }

    // region Build graphs

    /**
     * Build Phase 1 graph: title generation.
     */
    private StateGraph buildPhase1Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();

        return new StateGraph(keyStrategyFactory)
                .addNode("title_generator", node_async(titleGeneratorAgent))
                .addEdge(START, "title_generator")
                .addEdge("title_generator", END);
    }

    /**
     * Build Phase 2 graph: outline generation.
     */
    private StateGraph buildPhase2Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();

        return new StateGraph(keyStrategyFactory)
                .addNode("outline_generator", node_async(outlineGeneratorAgent))
                .addEdge(START, "outline_generator")
                .addEdge("outline_generator", END);
    }

    /**
     * Build Phase 3 graph: content + image generation (sequential).
     * Flow: content generation -> image requirement analysis -> parallel image generation -> content merge.
     */
    private StateGraph buildPhase3Graph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = createKeyStrategyFactory();

        return new StateGraph(keyStrategyFactory)
                // Node definitions
                .addNode("content_generator", node_async(contentGeneratorAgent))
                .addNode("image_analyzer", node_async(imageAnalyzerAgent))
                .addNode("parallel_image_generator", node_async(parallelImageGenerator))
                .addNode("content_merger", node_async(contentMergerAgent))
                // Edge definitions: sequential execution
                .addEdge(START, "content_generator")
                .addEdge("content_generator", "image_analyzer")
                .addEdge("image_analyzer", "parallel_image_generator")
                .addEdge("parallel_image_generator", "content_merger")
                .addEdge("content_merger", END);
    }

    /**
     * Create state key strategy factory.
     * All keys use the replace strategy.
     */
    private KeyStrategyFactory createKeyStrategyFactory() {
        return () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(KEY_TASK_ID, new ReplaceStrategy());
            strategies.put(KEY_TOPIC, new ReplaceStrategy());
            strategies.put(KEY_STYLE, new ReplaceStrategy());
            strategies.put(KEY_USER_DESCRIPTION, new ReplaceStrategy());
            strategies.put(KEY_MAIN_TITLE, new ReplaceStrategy());
            strategies.put(KEY_SUB_TITLE, new ReplaceStrategy());
            strategies.put(KEY_TITLE_OPTIONS, new ReplaceStrategy());
            strategies.put(KEY_OUTLINE, new ReplaceStrategy());
            strategies.put(KEY_CONTENT, new ReplaceStrategy());
            strategies.put(KEY_CONTENT_WITH_PLACEHOLDERS, new ReplaceStrategy());
            strategies.put(KEY_IMAGE_REQUIREMENTS, new ReplaceStrategy());
            strategies.put(KEY_IMAGES, new ReplaceStrategy());
            strategies.put(KEY_FULL_CONTENT, new ReplaceStrategy());
            strategies.put(KEY_ENABLED_IMAGE_METHODS, new ReplaceStrategy());
            return strategies;
        };
    }

    // endregion
}
