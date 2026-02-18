package com.zxuhan.template.agent.tools;

import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import com.zxuhan.template.service.ImageServiceStrategy;
import com.zxuhan.template.utils.GsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.Serializable;

/**
 * Image generation tool.
 * Wraps ImageServiceStrategy for use by agents.
 */
@Component
@Slf4j
public class ImageGenerationTool {

    @Resource
    private ImageServiceStrategy imageServiceStrategy;

    /**
     * Generate or search for an image based on the given requirements.
     *
     * @param imageSource  image source type (PEXELS/NANO_BANANA/MERMAID/ICONIFY/EMOJI_PACK/SVG_DIAGRAM)
     * @param keywords     search keywords (for image library retrieval)
     * @param prompt       AI image generation prompt or Mermaid/SVG code
     * @param position     image position index
     * @param type         image type (cover/section)
     * @param sectionTitle section title
     * @return JSON string of the image generation result
     */
    @Tool(description = "Generate or search for an image based on requirements. Supports multiple sources: PEXELS (real photos), NANO_BANANA (AI-generated), MERMAID (flowcharts), ICONIFY (icons), EMOJI_PACK (memes), SVG_DIAGRAM (conceptual diagrams)")
    public String generateImage(
            @ToolParam(description = "Image source type: PEXELS/NANO_BANANA/MERMAID/ICONIFY/EMOJI_PACK/SVG_DIAGRAM") String imageSource,
            @ToolParam(description = "Search keywords (for PEXELS/ICONIFY/EMOJI_PACK)") String keywords,
            @ToolParam(description = "AI image generation prompt or diagram code (for NANO_BANANA/MERMAID/SVG_DIAGRAM)") String prompt,
            @ToolParam(description = "Image position index; 1 for cover, others follow section order") Integer position,
            @ToolParam(description = "Image type: cover or section") String type,
            @ToolParam(description = "Corresponding section title; leave empty for cover images") String sectionTitle) {

        log.info("ImageGenerationTool start: imageSource={}, position={}, type={}",
                imageSource, position, type);

        try {
            // Build image request
            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(keywords)
                    .prompt(prompt)
                    .position(position)
                    .type(type)
                    .build();

            // Use unified upload-to-COS method
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(imageSource, imageRequest);
            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            // Build return result
            ImageGenerationResult generationResult = new ImageGenerationResult();
            generationResult.setPosition(position);
            generationResult.setUrl(cosUrl);
            generationResult.setMethod(method.getValue());
            generationResult.setKeywords(keywords);
            generationResult.setSectionTitle(sectionTitle);
            generationResult.setDescription(type);
            generationResult.setSuccess(true);

            log.info("ImageGenerationTool succeeded: position={}, method={}, cosUrl={}",
                    position, method.getValue(), cosUrl);

            return GsonUtils.toJson(generationResult);

        } catch (Exception e) {
            log.error("ImageGenerationTool failed: imageSource={}, position={}", imageSource, position, e);

            // Return failure result
            ImageGenerationResult failResult = new ImageGenerationResult();
            failResult.setPosition(position);
            failResult.setSuccess(false);
            failResult.setError(e.getMessage());
            failResult.setSectionTitle(sectionTitle);

            return GsonUtils.toJson(failResult);
        }
    }

    /**
     * Generate an image directly (internal use, bypasses agent).
     *
     * @param imageSource   image source
     * @param keywords      search keywords
     * @param prompt        generation prompt
     * @param position      position index
     * @param type          image type
     * @param sectionTitle  section title
     * @param placeholderId placeholder ID
     * @return image generation result
     */
    public ImageGenerationResult generateImageDirect(String imageSource, String keywords, String prompt,
                                                      Integer position, String type, String sectionTitle,
                                                      String placeholderId) {
        try {
            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(keywords)
                    .prompt(prompt)
                    .position(position)
                    .type(type)
                    .build();

            // Use unified upload-to-COS method
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(imageSource, imageRequest);
            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            ImageGenerationResult generationResult = new ImageGenerationResult();
            generationResult.setPosition(position);
            generationResult.setUrl(cosUrl);
            generationResult.setMethod(method.getValue());
            generationResult.setKeywords(keywords);
            generationResult.setSectionTitle(sectionTitle);
            generationResult.setDescription(type);
            generationResult.setPlaceholderId(placeholderId);
            generationResult.setSuccess(true);

            return generationResult;

        } catch (Exception e) {
            log.error("Image generation failed: imageSource={}, position={}", imageSource, position, e);

            ImageGenerationResult failResult = new ImageGenerationResult();
            failResult.setPosition(position);
            failResult.setSuccess(false);
            failResult.setError(e.getMessage());
            failResult.setSectionTitle(sectionTitle);
            failResult.setPlaceholderId(placeholderId);

            return failResult;
        }
    }

    /**
     * Image generation result.
     */
    @Data
    public static class ImageGenerationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
        private String placeholderId;
        private boolean success;
        private String error;
    }
}
