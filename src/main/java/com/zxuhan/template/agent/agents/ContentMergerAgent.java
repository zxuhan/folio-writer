package com.zxuhan.template.agent.agents;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.zxuhan.template.agent.tools.ImageGenerationTool;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Content merger agent.
 * Inserts images into the article body at the appropriate positions.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ContentMergerAgent implements NodeAction {

    public static final String INPUT_CONTENT = "content";
    public static final String INPUT_IMAGES = "images";
    public static final String OUTPUT_FULL_CONTENT = "fullContent";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String content = state.value(INPUT_CONTENT)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Missing content parameter"));

        @SuppressWarnings("unchecked")
        List<ArticleState.ImageResult> images = state.value(INPUT_IMAGES)
                .map(v -> {
                    if (v instanceof List) {
                        List<?> list = (List<?>) v;
                        if (list.isEmpty()) {
                            return new ArrayList<ArticleState.ImageResult>();
                        }
                        // Check element type
                        if (list.get(0) instanceof ArticleState.ImageResult) {
                            return (List<ArticleState.ImageResult>) v;
                        }
                        // Attempt conversion
                        return convertToImageResults(list);
                    }
                    return new ArrayList<ArticleState.ImageResult>();
                })
                .orElse(new ArrayList<>());

        log.info("ContentMergerAgent start: contentLength={}, imageCount={}", content.length(), images.size());

        String fullContent = mergeImagesIntoContent(content, images);

        log.info("ContentMergerAgent completed: fullContentLength={}", fullContent.length());

        return Map.of(OUTPUT_FULL_CONTENT, fullContent);
    }

    /**
     * Insert images into the article body using placeholder replacement.
     */
    private String mergeImagesIntoContent(String content, List<ArticleState.ImageResult> images) {
        if (images == null || images.isEmpty()) {
            return content;
        }

        String fullContent = content;

        // Replace each image placeholder with the actual image markdown
        for (ArticleState.ImageResult image : images) {
            String placeholder = image.getPlaceholderId();
            log.info("Processing image: position={}, placeholderId={}, url={}",
                    image.getPosition(), placeholder, image.getUrl());

            if (placeholder != null && !placeholder.isEmpty()) {
                String description = image.getDescription() != null ? image.getDescription() : "image";
                String imageMarkdown = "![" + description + "](" + image.getUrl() + ")";

                if (fullContent.contains(placeholder)) {
                    fullContent = fullContent.replace(placeholder, imageMarkdown);
                    log.info("Placeholder replaced successfully: {} -> {}", placeholder, imageMarkdown.substring(0, Math.min(50, imageMarkdown.length())));
                } else {
                    log.warn("Placeholder not found in content: {}", placeholder);
                }
            } else {
                log.warn("Image at position={} has an empty placeholderId", image.getPosition());
            }
        }

        return fullContent;
    }

    /**
     * Convert a raw list to a list of ImageResult objects.
     */
    private List<ArticleState.ImageResult> convertToImageResults(List<?> list) {
        List<ArticleState.ImageResult> results = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ArticleState.ImageResult) {
                results.add((ArticleState.ImageResult) item);
            } else if (item instanceof ImageGenerationTool.ImageGenerationResult) {
                // Convert from ImageGenerationTool.ImageGenerationResult
                ImageGenerationTool.ImageGenerationResult genResult =
                        (ImageGenerationTool.ImageGenerationResult) item;
                if (genResult.isSuccess()) {
                    ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
                    imageResult.setPosition(genResult.getPosition());
                    imageResult.setUrl(genResult.getUrl());
                    imageResult.setMethod(genResult.getMethod());
                    imageResult.setKeywords(genResult.getKeywords());
                    imageResult.setSectionTitle(genResult.getSectionTitle());
                    imageResult.setDescription(genResult.getDescription());
                    imageResult.setPlaceholderId(genResult.getPlaceholderId());
                    results.add(imageResult);
                }
            } else if (item instanceof Map) {
                // Convert from Map
                String json = GsonUtils.toJson(item);
                ArticleState.ImageResult imageResult = GsonUtils.fromJson(json, ArticleState.ImageResult.class);
                if (imageResult.getUrl() != null) {
                    results.add(imageResult);
                }
            }
        }
        return results;
    }
}
