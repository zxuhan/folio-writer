package com.zxuhan.template.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ImageConfig;
import com.google.genai.types.Part;
import com.zxuhan.template.config.NanoBananaConfig;
import com.zxuhan.template.model.dto.image.ImageData;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import static com.zxuhan.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Nano Banana (Gemini native image generation) service.
 * Generates images using Gemini 2.5 Flash Image or Gemini 3 Pro Image models.
 */
@Service
@Slf4j
public class NanoBananaService implements ImageSearchService {

    @Resource
    private NanoBananaConfig nanoBananaConfig;

    @Override
    public String searchImage(String keywords) {
        // This method is deprecated; use getImageData() instead.
        // Return null; upload logic is handled by ImageServiceStrategy.
        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        String prompt = request.getEffectiveParam(true);
        return generateImageData(prompt);
    }

    /**
     * Generate image data from a prompt
     *
     * @param prompt image generation prompt
     * @return ImageData containing image bytes, or null on failure
     */
    public ImageData generateImageData(String prompt) {
        try {
            // Explicitly set API key via builder
            Client genaiClient = Client.builder()
                    .apiKey(nanoBananaConfig.getApiKey())
                    .build();

            try {
                // Build image config
                ImageConfig.Builder imageConfigBuilder = ImageConfig.builder()
                        .aspectRatio(nanoBananaConfig.getAspectRatio());

                // Gemini 3 Pro Image supports higher resolution
                String model = nanoBananaConfig.getModel();
                if (model != null && model.contains("gemini-3-pro")) {
                    imageConfigBuilder.imageSize(nanoBananaConfig.getImageSize());
                }

                // Build generation config
                GenerateContentConfig config = GenerateContentConfig.builder()
                        .responseModalities("TEXT", "IMAGE")
                        .imageConfig(imageConfigBuilder.build())
                        .build();

                log.info("Nano Banana start generating image, model={}, prompt={}", model, prompt);

                // Call Gemini API to generate image
                GenerateContentResponse response = genaiClient.models.generateContent(
                        model != null ? model : "gemini-2.5-flash-image",
                        prompt,
                        config);

                // Extract image data from response
                if (response.parts() != null) {
                    for (Part part : response.parts()) {
                        if (part.inlineData().isPresent()) {
                            var blob = part.inlineData().get();
                            if (blob.data().isPresent()) {
                                byte[] imageBytes = blob.data().get();
                                String mimeType = blob.mimeType().orElse("image/png");

                                log.info("Nano Banana image generated, size={} bytes, mimeType={}",
                                        imageBytes.length, mimeType);

                                return ImageData.fromBytes(imageBytes, mimeType);
                            }
                        }
                    }
                }

                log.warn("Nano Banana did not generate an image, prompt={}", prompt);
                return null;

            } finally {
                genaiClient.close();
            }
        } catch (Exception e) {
            log.error("Nano Banana image generation failed, prompt={}", prompt, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.NANO_BANANA;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
