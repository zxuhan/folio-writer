package com.zxuhan.template.agent.agents;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.google.gson.reflect.TypeToken;
import com.zxuhan.template.constant.PromptConstant;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import com.zxuhan.template.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Image requirement analysis agent.
 * Analyzes article content and produces a list of image requirements.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageAnalyzerAgent implements NodeAction {

    private final ChatModel chatModel;

    public static final String INPUT_MAIN_TITLE = "mainTitle";
    public static final String INPUT_CONTENT = "content";
    public static final String INPUT_ENABLED_IMAGE_METHODS = "enabledImageMethods";
    public static final String OUTPUT_CONTENT_WITH_PLACEHOLDERS = "contentWithPlaceholders";
    public static final String OUTPUT_IMAGE_REQUIREMENTS = "imageRequirements";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String mainTitle = state.value(INPUT_MAIN_TITLE)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Missing main title parameter"));

        String content = state.value(INPUT_CONTENT)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Missing content parameter"));

        @SuppressWarnings("unchecked")
        List<String> enabledMethods = state.value(INPUT_ENABLED_IMAGE_METHODS)
                .map(v -> {
                    if (v instanceof List) {
                        return (List<String>) v;
                    }
                    return null;
                })
                .orElse(null);

        log.info("ImageAnalyzerAgent start: mainTitle={}, enabledMethods={}", mainTitle, enabledMethods);

        // Build description of available image methods
        String availableMethods = buildAvailableMethodsDescription(enabledMethods);
        // Build detailed usage guide for each allowed image method
        String methodUsageGuide = buildMethodUsageGuide(enabledMethods);

        // Build prompt
        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{content}", content)
                .replace("{availableMethods}", availableMethods)
                .replace("{methodUsageGuide}", methodUsageGuide);

        // Call LLM
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String responseContent = response.getResult().getOutput().getText();

        // Parse result (new format: contains contentWithPlaceholders and imageRequirements)
        ArticleState.Agent4Result agent4Result = GsonUtils.fromJson(
                GsonUtils.unwrapJson(responseContent),
                ArticleState.Agent4Result.class
        );

        // Validate and filter image requirements
        List<ArticleState.ImageRequirement> validatedRequirements = validateAndFilterImageRequirements(
                agent4Result.getImageRequirements(),
                enabledMethods
        );

        log.info("ImageAnalyzerAgent completed: requirementCount={}, validatedCount={}, placeholders inserted into content",
                agent4Result.getImageRequirements().size(), validatedRequirements.size());

        // Return result: contentWithPlaceholders, content (updated with placeholders), imageRequirements
        return Map.of(
                OUTPUT_CONTENT_WITH_PLACEHOLDERS, agent4Result.getContentWithPlaceholders(),
                INPUT_CONTENT, agent4Result.getContentWithPlaceholders(), // pass placeholder-embedded content to downstream nodes
                OUTPUT_IMAGE_REQUIREMENTS, validatedRequirements
        );
    }

    /**
     * Build a description of the available image methods.
     */
    private String buildAvailableMethodsDescription(List<String> enabledMethods) {
        if (enabledMethods == null || enabledMethods.isEmpty()) {
            return getAllMethodsDescription();
        }

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
     * Get the full description of all image methods.
     */
    private String getAllMethodsDescription() {
        return """
               - PEXELS: suitable for real-world scenes, product photos, portraits, and nature photography
               - NANO_BANANA: suitable for creative illustrations, infographics, text rendering, abstract concepts, and AI-generated artistic images
               - MERMAID: suitable for flowcharts, architecture diagrams, sequence diagrams, relationship diagrams, and Gantt charts
               - ICONIFY: suitable for icons, symbols, and small decorative icons (e.g. arrows, checkmarks, stars, hearts)
               - EMOJI_PACK: suitable for meme-style images and humorous/lighthearted illustrations
               - SVG_DIAGRAM: suitable for conceptual diagrams, mind-map-style visuals, and logic relationship displays (no precise data required)
               """;
    }

    /**
     * Get the usage description for a specific image method.
     */
    private String getMethodUsageDescription(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "suitable for real-world scenes, product photos, portraits, and nature photography";
            case NANO_BANANA -> "suitable for creative illustrations, infographics, text rendering, abstract concepts, and AI-generated artistic images";
            case MERMAID -> "suitable for flowcharts, architecture diagrams, sequence diagrams, relationship diagrams, and Gantt charts";
            case ICONIFY -> "suitable for icons, symbols, and small decorative icons (e.g. arrows, checkmarks, stars, hearts)";
            case EMOJI_PACK -> "suitable for meme-style images and humorous/lighthearted illustrations";
            case SVG_DIAGRAM -> "suitable for conceptual diagrams, mind-map-style visuals, and logic relationship displays (no precise data required)";
            default -> method.getDescription();
        };
    }

    /**
     * Build the detailed usage guide for allowed image methods only.
     */
    private String buildMethodUsageGuide(List<String> enabledMethods) {
        // If no restriction, include all methods
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
     * Get the detailed usage guide for a single image method.
     */
    private String getMethodDetailedGuide(String method) {
        return switch (method) {
            case "PEXELS" -> """
                    - PEXELS: provide English search keywords (keywords) that are accurate and specific. Leave prompt empty.""";
            case "NANO_BANANA" -> """
                    - NANO_BANANA: provide a detailed English image generation prompt describing the scene, style, and details. Leave keywords empty.""";
            case "MERMAID" -> """
                    - MERMAID: generate complete Mermaid code (e.g. flowchart, architecture diagram) in the prompt field. Leave keywords empty.""";
            case "ICONIFY" -> """
                    - ICONIFY: provide English icon keywords (keywords), e.g.: check, arrow, star, heart. Leave prompt empty.""";
            case "EMOJI_PACK" -> """
                    - EMOJI_PACK: provide keywords (in English or Chinese) describing the meme content. Leave prompt empty. The system will automatically append the meme search suffix.""";
            case "SVG_DIAGRAM" -> """
                    - SVG_DIAGRAM: describe the diagram requirements in the prompt field (in English), specifying the concept and relationships to convey. Leave keywords empty.
                      Example: Draw a mind-map-style diagram with "Self-Discipline" at the center and four branches: Habits, Environment, Feedback, System.""";
            default -> null;
        };
    }

    /**
     * Validate and filter image requirements against the allowed methods.
     */
    private List<ArticleState.ImageRequirement> validateAndFilterImageRequirements(
            List<ArticleState.ImageRequirement> requirements,
            List<String> enabledMethods) {

        if (enabledMethods == null || enabledMethods.isEmpty()) {
            return requirements;
        }

        List<ArticleState.ImageRequirement> validatedRequirements = new ArrayList<>();

        for (ArticleState.ImageRequirement req : requirements) {
            String imageSource = req.getImageSource();

            if (enabledMethods.contains(imageSource)) {
                validatedRequirements.add(req);
                log.debug("Image requirement validated: position={}, imageSource={}", req.getPosition(), imageSource);
            } else {
                log.warn("Image requirement rejected (not in allowed methods): position={}, imageSource={}, enabledMethods={}",
                        req.getPosition(), imageSource, enabledMethods);

                // Attempt to replace with an allowed method
                if (!enabledMethods.isEmpty()) {
                    String fallbackSource = enabledMethods.get(0);
                    req.setImageSource(fallbackSource);
                    validatedRequirements.add(req);
                    log.info("Image requirement replaced with allowed method: position={}, fallback={}",
                            req.getPosition(), fallbackSource);
                }
            }
        }

        return validatedRequirements;
    }
}
