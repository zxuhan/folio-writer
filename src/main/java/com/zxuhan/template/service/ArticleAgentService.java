package com.zxuhan.template.service;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zxuhan.template.annotation.AgentExecution;
import com.zxuhan.template.constant.PromptConstant;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ArticleStyleEnum;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import com.zxuhan.template.model.enums.SseMessageTypeEnum;
import com.zxuhan.template.utils.GsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Article agent orchestration service
 */
@Service
@Slf4j
public class ArticleAgentService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ImageServiceStrategy imageServiceStrategy;

    /**
     * Phase 1: Generate title options (3-5 candidates)
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase1_GenerateTitles(ArticleState state, Consumer<String> streamHandler) {
        try {
            // Agent 1: generate title options
            log.info("Phase 1: Start generating title options, taskId={}", state.getTaskId());
            // Call through proxy to ensure AOP takes effect
            getProxy().agent1GenerateTitleOptions(state);
            streamHandler.accept(SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            log.info("Phase 1: Title options generated, taskId={}, optionsCount={}",
                state.getTaskId(), state.getTitleOptions().size());
        } catch (Exception e) {
            log.error("Phase 1: Title options generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Title options generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Phase 2: Generate outline (after user selects a title)
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase2_GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        try {
            // Agent 2: generate outline (streaming output)
            log.info("Phase 2: Start generating outline, taskId={}", state.getTaskId());
            // Call through proxy to ensure AOP takes effect
            getProxy().agent2GenerateOutline(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            log.info("Phase 2: Outline generated, taskId={}", state.getTaskId());
        } catch (Exception e) {
            log.error("Phase 2: Outline generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Outline generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Phase 3: Generate content and images (after user confirms outline)
     *
     * @param state         article state
     * @param streamHandler streaming output handler
     */
    public void executePhase3_GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        try {
            // Get proxy object
            ArticleAgentService proxy = getProxy();

            // Agent 3: generate content (streaming output)
            log.info("Phase 3: Start generating content, taskId={}", state.getTaskId());
            proxy.agent3GenerateContent(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT3_COMPLETE.getValue());

            // Agent 4: analyze image requirements
            log.info("Phase 3: Start analyzing image requirements, taskId={}", state.getTaskId());
            proxy.agent4AnalyzeImageRequirements(state);
            streamHandler.accept(SseMessageTypeEnum.AGENT4_COMPLETE.getValue());

            // Agent 5: generate images
            log.info("Phase 3: Start generating images, taskId={}", state.getTaskId());
            proxy.agent5GenerateImages(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT5_COMPLETE.getValue());

            // Merge images into content
            log.info("Phase 3: Start merging images into content, taskId={}", state.getTaskId());
            proxy.mergeImagesIntoContent(state);
            streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());

            log.info("Phase 3: Content generation complete, taskId={}", state.getTaskId());
        } catch (Exception e) {
            log.error("Phase 3: Content generation failed, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("Content generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Agent 1: Generate title options (3-5 candidates)
     */
    @AgentExecution(value = "agent1_generate_titles", description = "Generate title options")
    public void agent1GenerateTitleOptions(ArticleState state) {
        String prompt = PromptConstant.AGENT1_TITLE_PROMPT
                .replace("{topic}", state.getTopic())
                + getStylePrompt(state.getStyle());

        String content = callLlm(prompt);
        List<ArticleState.TitleOption> titleOptions = parseJsonListResponse(
                content,
                new TypeToken<List<ArticleState.TitleOption>>(){},
                "title options"
        );
        state.setTitleOptions(titleOptions);
        log.info("Agent 1: Title options generated, optionsCount={}", titleOptions.size());
    }

    /**
     * Agent 2: Generate outline (streaming output)
     */
    @AgentExecution(value = "agent2_generate_outline", description = "Generate article outline")
    public void agent2GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        // Build prompt, conditionally inserting the user description section
        String descriptionSection = "";
        if (state.getUserDescription() != null && !state.getUserDescription().trim().isEmpty()) {
            descriptionSection = PromptConstant.AGENT2_DESCRIPTION_SECTION
                    .replace("{userDescription}", state.getUserDescription());
        }

        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                .replace("{descriptionSection}", descriptionSection)
                + getStylePrompt(state.getStyle());

        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT2_STREAMING);
        ArticleState.OutlineResult outlineResult = parseJsonResponse(content, ArticleState.OutlineResult.class, "outline");
        state.setOutline(outlineResult);
        log.info("Agent 2: Outline generated, sections={}", outlineResult.getSections().size());
    }

    /**
     * Agent 3: Generate content (streaming output)
     */
    @AgentExecution(value = "agent3_generate_content", description = "Generate article content")
    public void agent3GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        String outlineText = GsonUtils.toJson(state.getOutline().getSections());
        String prompt = PromptConstant.AGENT3_CONTENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                .replace("{outline}", outlineText)
                + getStylePrompt(state.getStyle());

        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT3_STREAMING);
        state.setContent(content);
        log.info("Agent 3: Content generated, length={}", content.length());
    }

    /**
     * Agent 4: Analyze image requirements (insert placeholders into content)
     */
    @AgentExecution(value = "agent4_analyze_image_requirements", description = "Analyze image requirements")
    public void agent4AnalyzeImageRequirements(ArticleState state) {
        // Build description of available image methods
        String availableMethods = buildAvailableMethodsDescription(state.getEnabledImageMethods());
        // Build detailed usage guide for each allowed image method
        String methodUsageGuide = buildMethodUsageGuide(state.getEnabledImageMethods());

        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent())
                .replace("{availableMethods}", availableMethods)
                .replace("{methodUsageGuide}", methodUsageGuide);

        String content = callLlm(prompt);
        ArticleState.Agent4Result agent4Result = parseJsonResponse(
                content,
                ArticleState.Agent4Result.class,
                "image requirements"
        );

        // Update content to the version with placeholders
        state.setContent(agent4Result.getContentWithPlaceholders());

        // Validate and filter image requirements to ensure all imageSources are in the allowed list
        List<ArticleState.ImageRequirement> validatedRequirements = validateAndFilterImageRequirements(
                agent4Result.getImageRequirements(),
                state.getEnabledImageMethods()
        );

        state.setImageRequirements(validatedRequirements);
        log.info("Agent 4: Image requirements analyzed, count={}, validated={}, placeholders inserted into content",
                agent4Result.getImageRequirements().size(), validatedRequirements.size());
    }

    /**
     * Agent 5: Generate images (sequential, supports mixing multiple image methods, unified upload to COS)
     */
    @AgentExecution(value = "agent5_generate_images", description = "Generate images")
    public void agent5GenerateImages(ArticleState state, Consumer<String> streamHandler) {
        List<ArticleState.ImageResult> imageResults = new ArrayList<>();

        for (ArticleState.ImageRequirement requirement : state.getImageRequirements()) {
            String imageSource = requirement.getImageSource();
            log.info("Agent 5: Fetching image, position={}, imageSource={}, keywords={}",
                    requirement.getPosition(), imageSource, requirement.getKeywords());

            // Build image request object
            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(requirement.getKeywords())
                    .prompt(requirement.getPrompt())
                    .position(requirement.getPosition())
                    .type(requirement.getType())
                    .build();

            // Use strategy pattern to get image and upload to COS
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(imageSource, imageRequest);

            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            // Create image result (URL is already a COS address)
            ArticleState.ImageResult imageResult = buildImageResult(requirement, cosUrl, method);
            imageResults.add(imageResult);

            // Push single image complete event
            String imageCompleteMessage = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix() + GsonUtils.toJson(imageResult);
            streamHandler.accept(imageCompleteMessage);

            log.info("Agent 5: Image fetched and uploaded, position={}, method={}, cosUrl={}",
                    requirement.getPosition(), method.getValue(), cosUrl);
        }

        state.setImages(imageResults);
        log.info("Agent 5: All images generated and uploaded, count={}", imageResults.size());
    }

    /**
     * Merge images into content by replacing placeholders with actual image tags
     */
    @AgentExecution(value = "agent6_merge_content", description = "Merge images into content")
    public void mergeImagesIntoContent(ArticleState state) {
        String content = state.getContent();
        List<ArticleState.ImageResult> images = state.getImages();

        if (images == null || images.isEmpty()) {
            state.setFullContent(content);
            return;
        }

        String fullContent = content;

        // Replace each placeholder with the actual image Markdown
        for (ArticleState.ImageResult image : images) {
            String placeholder = image.getPlaceholderId();
            if (placeholder != null && !placeholder.isEmpty()) {
                String imageMarkdown = "![" + image.getDescription() + "](" + image.getUrl() + ")";
                fullContent = fullContent.replace(placeholder, imageMarkdown);
            }
        }

        state.setFullContent(fullContent);
        log.info("Image merge complete, fullContentLength={}", fullContent.length());
    }

    // region Helper methods

    /**
     * Call LLM (non-streaming)
     */
    private String callLlm(String prompt) {
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        return response.getResult().getOutput().getText();
    }

    /**
     * Call LLM (streaming output)
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> streamHandler, SseMessageTypeEnum messageType) {
        StringBuilder contentBuilder = new StringBuilder();

        Flux<ChatResponse> streamResponse = chatModel.stream(new Prompt(new UserMessage(prompt)));

        streamResponse
                .doOnNext(response -> {
                    String chunk = response.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        contentBuilder.append(chunk);
                        streamHandler.accept(messageType.getStreamingPrefix() + chunk);
                    }
                })
                .doOnError(error -> log.error("LLM streaming call failed, messageType={}", messageType, error))
                .blockLast();

        return contentBuilder.toString();
    }

    /**
     * Parse JSON response
     */
    private <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        try {
            return GsonUtils.fromJson(GsonUtils.unwrapJson(content), clazz);
        } catch (JsonSyntaxException e) {
            log.error("{} parsing failed, content={}", name, content, e);
            throw new RuntimeException(name + " parsing failed");
        }
    }

    /**
     * Parse JSON list response
     */
    private <T> T parseJsonListResponse(String content, TypeToken<T> typeToken, String name) {
        try {
            return GsonUtils.fromJson(GsonUtils.unwrapJson(content), typeToken);
        } catch (JsonSyntaxException e) {
            log.error("{} parsing failed, content={}", name, content, e);
            throw new RuntimeException(name + " parsing failed");
        }
    }

    /**
     * Build image result
     */
    private ArticleState.ImageResult buildImageResult(ArticleState.ImageRequirement requirement,
                                                       String imageUrl, 
                                                       ImageMethodEnum method) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(requirement.getPosition());
        imageResult.setUrl(imageUrl);
        imageResult.setMethod(method.getValue());
        imageResult.setKeywords(requirement.getKeywords());
        imageResult.setSectionTitle(requirement.getSectionTitle());
        imageResult.setDescription(requirement.getType());
        imageResult.setPlaceholderId(requirement.getPlaceholderId());
        return imageResult;
    }

    /**
     * Build description of available image methods
     */
    private String buildAvailableMethodsDescription(List<String> enabledMethods) {
        // If empty or null, all methods are supported
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            return getAllMethodsDescription();
        }

        // Only describe allowed methods
        StringBuilder sb = new StringBuilder();
        for (String method : enabledMethods) {
            ImageMethodEnum methodEnum = ImageMethodEnum.getByValue(method);
            if (methodEnum != null && !methodEnum.isFallback()) {
                sb.append("   - ").append(methodEnum.getValue())
                        .append(": ").append(getMethodUsageDescription(methodEnum))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get complete description of all image methods
     */
    private String getAllMethodsDescription() {
        return """
               - PEXELS: suitable for real-world scenes, product photos, portraits, nature landscapes, and other realistic images
               - NANO_BANANA: suitable for creative illustrations, infographics, text rendering, abstract concepts, artistic styles, and other AI-generated images
               - MERMAID: suitable for flowcharts, architecture diagrams, sequence diagrams, relationship diagrams, Gantt charts, and other structured charts
               - ICONIFY: suitable for icons, symbols, small decorative icons (e.g., arrows, checkmarks, stars, hearts)
               - EMOJI_PACK: suitable for memes, funny images, and light-hearted humorous illustrations
               - SVG_DIAGRAM: suitable for conceptual diagrams, mind-map-style visuals, and logical relationship illustrations (not for precise data)
               """;
    }

    /**
     * Get usage description for an image method
     */
    private String getMethodUsageDescription(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "suitable for real-world scenes, product photos, portraits, nature landscapes, and other realistic images";
            case NANO_BANANA -> "suitable for creative illustrations, infographics, text rendering, abstract concepts, artistic styles, and other AI-generated images";
            case MERMAID -> "suitable for flowcharts, architecture diagrams, sequence diagrams, relationship diagrams, Gantt charts, and other structured charts";
            case ICONIFY -> "suitable for icons, symbols, small decorative icons (e.g., arrows, checkmarks, stars, hearts)";
            case EMOJI_PACK -> "suitable for memes, funny images, and light-hearted humorous illustrations";
            case SVG_DIAGRAM -> "suitable for conceptual diagrams, mind-map-style visuals, and logical relationship illustrations (not for precise data)";
            default -> method.getDescription();
        };
    }

    /**
     * Build detailed usage guide for image methods (only for allowed methods)
     */
    private String buildMethodUsageGuide(List<String> enabledMethods) {
        // If no restriction, return guides for all methods
        List<String> methodsToInclude = (enabledMethods == null || enabledMethods.isEmpty())
                ? List.of("PEXELS", "NANO_BANANA", "MERMAID", "ICONIFY", "EMOJI_PACK", "SVG_DIAGRAM")
                : enabledMethods;

        StringBuilder sb = new StringBuilder();
        
        for (String method : methodsToInclude) {
            String guide = getMethodDetailedGuide(method);
            if (guide != null && !guide.isEmpty()) {
                sb.append(guide).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Get detailed usage guide for a single image method
     */
    private String getMethodDetailedGuide(String method) {
        return switch (method) {
            case "PEXELS" -> """
                    - PEXELS: Provide accurate, specific English search keywords (keywords). Leave prompt empty.""";
            case "NANO_BANANA" -> """
                    - NANO_BANANA: Provide a detailed English image generation prompt (prompt) describing scene, style, and details. Leave keywords empty.""";
            case "MERMAID" -> """
                    - MERMAID: Generate complete Mermaid code (e.g. flowchart, architecture diagram) in the prompt field. Leave keywords empty.""";
            case "ICONIFY" -> """
                    - ICONIFY: Provide English icon keywords (keywords), e.g.: check, arrow, star, heart. Leave prompt empty.""";
            case "EMOJI_PACK" -> """
                    - EMOJI_PACK: Provide keywords (keywords) describing the meme content. Leave prompt empty. The system automatically appends a meme search term.""";
            case "SVG_DIAGRAM" -> """
                    - SVG_DIAGRAM: Describe the diagram requirement in the prompt field (in English), explaining the concept and relationships to illustrate. Leave keywords empty.
                      Example: Draw a mind-map style diagram with "Self-discipline" at the center and 4 branches: Habits, Environment, Feedback, Systems""";
            default -> null;
        };
    }

    /**
     * Validate and filter image requirements.
     * Ensures all imageSources are in the allowed list.
     *
     * @param requirements   original image requirement list
     * @param enabledMethods allowed image method list
     * @return validated image requirement list
     */
    private List<ArticleState.ImageRequirement> validateAndFilterImageRequirements(
            List<ArticleState.ImageRequirement> requirements,
            List<String> enabledMethods) {

        // If no restriction, return all requirements
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            return requirements;
        }

        List<ArticleState.ImageRequirement> validatedRequirements = new ArrayList<>();

        for (ArticleState.ImageRequirement req : requirements) {
            String imageSource = req.getImageSource();

            // Check if imageSource is in the allowed list
            if (enabledMethods.contains(imageSource)) {
                validatedRequirements.add(req);
                log.debug("Image requirement validated, position={}, imageSource={}", req.getPosition(), imageSource);
            } else {
                log.warn("Image requirement filtered out (not in allowed list), position={}, imageSource={}, enabledMethods={}",
                        req.getPosition(), imageSource, enabledMethods);

                // Fall back to the first allowed method
                if (!enabledMethods.isEmpty()) {
                    String fallbackSource = enabledMethods.get(0);
                    req.setImageSource(fallbackSource);
                    validatedRequirements.add(req);
                    log.info("Image requirement replaced with allowed method, position={}, fallback={}",
                            req.getPosition(), fallbackSource);
                }
            }
        }

        return validatedRequirements;
    }

    /**
     * Get the additional prompt content for the given style
     *
     * @param style article style
     * @return style-specific prompt addition, or empty string if no style
     */
    private String getStylePrompt(String style) {
        if (style == null || style.isEmpty()) {
            return "";
        }
        
        ArticleStyleEnum styleEnum = ArticleStyleEnum.getEnumByValue(style);
        if (styleEnum == null) {
            return "";
        }
        
        return switch (styleEnum) {
            case TECH -> PromptConstant.STYLE_TECH_PROMPT;
            case EMOTIONAL -> PromptConstant.STYLE_EMOTIONAL_PROMPT;
            case EDUCATIONAL -> PromptConstant.STYLE_EDUCATIONAL_PROMPT;
            case HUMOROUS -> PromptConstant.STYLE_HUMOROUS_PROMPT;
        };
    }

    /**
     * AI-assisted outline modification
     *
     * @param mainTitle        main title
     * @param subTitle         subtitle
     * @param currentOutline   current outline
     * @param modifySuggestion user's modification suggestion
     * @return modified outline
     */
    @AgentExecution(value = "ai_modify_outline", description = "AI outline modification")
    public List<ArticleState.OutlineSection> aiModifyOutline(String mainTitle, String subTitle,
                                                             List<ArticleState.OutlineSection> currentOutline,
                                                             String modifySuggestion) {
        String currentOutlineJson = GsonUtils.toJson(currentOutline);

        String prompt = PromptConstant.AI_MODIFY_OUTLINE_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{subTitle}", subTitle)
                .replace("{currentOutline}", currentOutlineJson)
                .replace("{modifySuggestion}", modifySuggestion);

        String content = callLlm(prompt);
        ArticleState.OutlineResult outlineResult = parseJsonResponse(content, ArticleState.OutlineResult.class, "modified outline");

        log.info("AI outline modification complete, sectionsCount={}", outlineResult.getSections().size());
        return outlineResult.getSections();
    }

    /**
     * Get the proxy object of this class.
     * Used to resolve the Spring AOP self-invocation problem.
     */
    private ArticleAgentService getProxy() {
        try {
            return (ArticleAgentService) AopContext.currentProxy();
        } catch (IllegalStateException e) {
            // Fall back to this if proxy is unavailable
            log.warn("Failed to get AOP proxy, falling back to raw instance: {}", e.getMessage());
            return this;
        }
    }

    // endregion
}
