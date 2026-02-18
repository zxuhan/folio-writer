package com.zxuhan.template.agent.parallel;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.zxuhan.template.agent.context.StreamHandlerContext;
import com.zxuhan.template.agent.tools.ImageGenerationTool;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.enums.SseMessageTypeEnum;
import com.zxuhan.template.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Parallel image generator.
 * Groups image requirements by imageSource and executes different image generation tasks in parallel.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ParallelImageGenerator implements NodeAction {

    private final ImageGenerationTool imageGenerationTool;

    public static final String INPUT_IMAGE_REQUIREMENTS = "imageRequirements";
    public static final String OUTPUT_IMAGES = "images";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        @SuppressWarnings("unchecked")
        List<ArticleState.ImageRequirement> imageRequirements = state.value(INPUT_IMAGE_REQUIREMENTS)
                .map(v -> {
                    if (v instanceof List) {
                        List<?> list = (List<?>) v;
                        if (list.isEmpty()) {
                            return new ArrayList<ArticleState.ImageRequirement>();
                        }
                        if (list.get(0) instanceof ArticleState.ImageRequirement) {
                            return (List<ArticleState.ImageRequirement>) v;
                        }
                        // Attempt conversion
                        return convertToImageRequirements(list);
                    }
                    return new ArrayList<ArticleState.ImageRequirement>();
                })
                .orElse(new ArrayList<>());

        // Get streaming handler from ThreadLocal
        Consumer<String> streamHandler = StreamHandlerContext.get();

        log.info("ParallelImageGenerator start: imageRequirementCount={}", imageRequirements.size());

        if (imageRequirements.isEmpty()) {
            log.info("No image requirements found, skipping image generation");
            return Map.of(OUTPUT_IMAGES, new ArrayList<>());
        }

        // Group by imageSource
        Map<String, List<ArticleState.ImageRequirement>> groupedBySource = imageRequirements.stream()
                .collect(Collectors.groupingBy(ArticleState.ImageRequirement::getImageSource));

        log.info("Image requirements grouped by type: {}",
                groupedBySource.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().size()
                        )));

        // Execute image generation in parallel
        List<ArticleState.ImageResult> allImages = executeParallel(groupedBySource, streamHandler);

        // Sort by position
        allImages.sort((a, b) -> {
            Integer posA = a.getPosition() != null ? a.getPosition() : 0;
            Integer posB = b.getPosition() != null ? b.getPosition() : 0;
            return posA.compareTo(posB);
        });

        log.info("ParallelImageGenerator completed: successfully generated {} images", allImages.size());

        return Map.of(OUTPUT_IMAGES, allImages);
    }

    /**
     * Execute image generation tasks in parallel.
     * Different imageSource types run in parallel; tasks within the same type run sequentially.
     */
    private List<ArticleState.ImageResult> executeParallel(
            Map<String, List<ArticleState.ImageRequirement>> groupedBySource,
            Consumer<String> streamHandler) {

        // Use a thread-safe list to collect results
        CopyOnWriteArrayList<ArticleState.ImageResult> allImages = new CopyOnWriteArrayList<>();

        // Create an async task for each imageSource type
        List<CompletableFuture<Void>> futures = groupedBySource.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    String imageSource = entry.getKey();
                    List<ArticleState.ImageRequirement> requirements = entry.getValue();

                    log.info("Processing {} type images, count: {}", imageSource, requirements.size());

                    // Sequential execution within the same type
                    for (ArticleState.ImageRequirement req : requirements) {
                        try {
                            ImageGenerationTool.ImageGenerationResult result =
                                    imageGenerationTool.generateImageDirect(
                                            req.getImageSource(),
                                            req.getKeywords(),
                                            req.getPrompt(),
                                            req.getPosition(),
                                            req.getType(),
                                            req.getSectionTitle(),
                                            req.getPlaceholderId()
                                    );

                            if (result.isSuccess()) {
                                ArticleState.ImageResult imageResult = convertToImageResult(result);
                                allImages.add(imageResult);

                                // Push single image completion event
                                if (streamHandler != null) {
                                    String message = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix()
                                            + GsonUtils.toJson(imageResult);
                                    streamHandler.accept(message);
                                }

                                log.info("Image generated successfully: imageSource={}, position={}",
                                        imageSource, req.getPosition());
                            } else {
                                log.warn("Image generation failed: imageSource={}, position={}, error={}",
                                        imageSource, req.getPosition(), result.getError());
                            }
                        } catch (Exception e) {
                            log.error("Image generation exception: imageSource={}, position={}",
                                    imageSource, req.getPosition(), e);
                        }
                    }

                    log.info("Finished processing {} type images", imageSource);
                }))
                .toList();

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return new ArrayList<>(allImages);
    }

    /**
     * Convert ImageGenerationResult to ArticleState.ImageResult.
     */
    private ArticleState.ImageResult convertToImageResult(ImageGenerationTool.ImageGenerationResult genResult) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(genResult.getPosition());
        imageResult.setUrl(genResult.getUrl());
        imageResult.setMethod(genResult.getMethod());
        imageResult.setKeywords(genResult.getKeywords());
        imageResult.setSectionTitle(genResult.getSectionTitle());
        imageResult.setDescription(genResult.getDescription());
        imageResult.setPlaceholderId(genResult.getPlaceholderId());
        return imageResult;
    }

    /**
     * Convert a raw list to a list of ImageRequirement objects.
     */
    private List<ArticleState.ImageRequirement> convertToImageRequirements(List<?> list) {
        List<ArticleState.ImageRequirement> results = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ArticleState.ImageRequirement) {
                results.add((ArticleState.ImageRequirement) item);
            } else if (item instanceof Map) {
                String json = GsonUtils.toJson(item);
                ArticleState.ImageRequirement req = GsonUtils.fromJson(json, ArticleState.ImageRequirement.class);
                results.add(req);
            }
        }
        return results;
    }
}
