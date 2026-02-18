package com.zxuhan.template.service;

import com.zxuhan.template.model.dto.image.ImageData;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Image service strategy selector.
 * Selects the appropriate image service implementation based on the image source type.
 *
 * Design notes:
 * - Automatically registers all ImageSearchService implementations
 * - Automatically selects the correct parameter based on ImageMethodEnum metadata
 * - Supports service availability checks and automatic fallback
 * - Handles unified image upload to R2
 */
@Service
@Slf4j
public class ImageServiceStrategy {

    @Resource
    private List<ImageSearchService> imageSearchServices;

    @Resource
    private R2Service r2Service;

    /**
     * Image service map: ImageMethodEnum -> ImageSearchService
     */
    private final Map<ImageMethodEnum, ImageSearchService> serviceMap = new EnumMap<>(ImageMethodEnum.class);

    @PostConstruct
    public void init() {
        // Register all ImageSearchService implementations into the map
        for (ImageSearchService service : imageSearchServices) {
            ImageMethodEnum method = service.getMethod();
            serviceMap.put(method, service);
            log.info("Registered image service: {} -> {} (aiGenerated: {}, fallback: {})",
                    method.getValue(),
                    service.getClass().getSimpleName(),
                    method.isAiGenerated(),
                    method.isFallback());
        }
    }

    /**
     * Get image and upload to R2 (recommended method).
     * Handles upload logic uniformly for all image sources.
     *
     * @param imageSource image source
     * @param request     image request object
     * @return image result (containing public R2 URL)
     */
    public ImageResult getImageAndUpload(String imageSource, ImageRequest request) {
        ImageMethodEnum method = resolveMethod(imageSource);
        ImageSearchService service = serviceMap.get(method);

        if (service == null || !service.isAvailable()) {
            log.warn("Image service unavailable: {}, falling back", method);
            return handleFallbackWithUpload(request.getPosition());
        }

        try {
            // 1. Get image data
            ImageData imageData = service.getImageData(request);

            if (imageData == null || !imageData.isValid()) {
                log.warn("Image data retrieval failed, using fallback, method={}", method);
                return handleFallbackWithUpload(request.getPosition());
            }

            // 2. Upload to R2
            String folder = getFolderForMethod(method);
            String publicUrl = r2Service.uploadImageData(imageData, folder);

            if (publicUrl != null && !publicUrl.isEmpty()) {
                log.info("Image retrieved and uploaded, method={}, url={}", method, publicUrl);
                return new ImageResult(publicUrl, method);
            } else {
                log.warn("Image upload to R2 failed, using fallback, method={}", method);
                return handleFallbackWithUpload(request.getPosition());
            }
        } catch (Exception e) {
            log.error("Image retrieval and upload failed, method={}", method, e);
            return handleFallbackWithUpload(request.getPosition());
        }
    }

    /**
     * Get image by request
     *
     * @param imageSource image source
     * @param request     image request object
     * @return image result
     * @deprecated Use getImageAndUpload() instead
     */
    @Deprecated
    public ImageResult getImage(String imageSource, ImageRequest request) {
        ImageMethodEnum method = resolveMethod(imageSource);
        ImageSearchService service = serviceMap.get(method);

        if (service == null || !service.isAvailable()) {
            log.warn("Image service unavailable: {}, falling back", method);
            return handleFallback(request.getPosition());
        }

        String imageUrl = service.getImage(request);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            return new ImageResult(imageUrl, method);
        } else {
            log.warn("Image retrieval failed, using fallback, method={}", method);
            return handleFallback(request.getPosition());
        }
    }

    /**
     * Get image by source (legacy interface, no R2 upload)
     *
     * @param imageSource image source (PEXELS / NANO_BANANA, etc.)
     * @param keywords    keywords (for library search)
     * @param prompt      prompt (for AI generation)
     * @return image result
     * @deprecated Use getImageAndUpload() instead
     */
    @Deprecated
    public ImageResult getImage(String imageSource, String keywords, String prompt) {
        ImageRequest request = ImageRequest.builder()
                .keywords(keywords)
                .prompt(prompt)
                .build();
        return getImage(imageSource, request);
    }

    /**
     * Get R2 folder for the given image method
     */
    private String getFolderForMethod(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "pexels";
            case NANO_BANANA -> "nano-banana";
            case MERMAID -> "mermaid";
            case ICONIFY -> "iconify";
            case EMOJI_PACK -> "emoji-pack";
            case SVG_DIAGRAM -> "svg-diagram";
            case PICSUM -> "picsum";
        };
    }

    /**
     * Resolve image source, handling unknown values
     */
    private ImageMethodEnum resolveMethod(String imageSource) {
        ImageMethodEnum method = ImageMethodEnum.getByValue(imageSource);
        if (method == null) {
            log.warn("Unknown image source: {}, defaulting to {}", imageSource, ImageMethodEnum.getDefaultSearchMethod());
            return ImageMethodEnum.getDefaultSearchMethod();
        }
        return method;
    }

    /**
     * Handle fallback logic
     */
    private ImageResult handleFallback(Integer position) {
        int pos = position != null ? position : 1;
        String fallbackUrl = getFallbackImage(pos);
        return new ImageResult(fallbackUrl, ImageMethodEnum.getFallbackMethod());
    }

    /**
     * Handle fallback logic with R2 upload
     */
    private ImageResult handleFallbackWithUpload(Integer position) {
        int pos = position != null ? position : 1;
        String fallbackUrl = getFallbackImage(pos);

        // Also upload the fallback image to R2
        ImageData fallbackData = ImageData.fromUrl(fallbackUrl);
        String publicUrl = r2Service.uploadImageData(fallbackData, "fallback");

        // If upload fails, use the original URL
        String finalUrl = (publicUrl != null && !publicUrl.isEmpty()) ? publicUrl : fallbackUrl;
        return new ImageResult(finalUrl, ImageMethodEnum.getFallbackMethod());
    }

    /**
     * Get image service for the specified method
     *
     * @param method image method
     * @return image service, or null if not found
     */
    public ImageSearchService getService(ImageMethodEnum method) {
        return serviceMap.get(method);
    }

    /**
     * Get fallback image
     *
     * @param position position index
     * @return fallback image URL
     */
    public String getFallbackImage(int position) {
        // Prefer the fallback from the registered default service
        ImageSearchService defaultService = serviceMap.get(ImageMethodEnum.getDefaultSearchMethod());
        if (defaultService != null) {
            return defaultService.getFallbackImage(position);
        }
        return String.format("https://picsum.photos/800/600?random=%d", position);
    }

    /**
     * Get all registered image service types
     */
    public List<ImageMethodEnum> getRegisteredMethods() {
        return List.copyOf(serviceMap.keySet());
    }

    /**
     * Image retrieval result
     */
    public static class ImageResult {
        private final String url;
        private final ImageMethodEnum method;

        public ImageResult(String url, ImageMethodEnum method) {
            this.url = url;
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public ImageMethodEnum getMethod() {
            return method;
        }

        public boolean isSuccess() {
            return url != null && !url.isEmpty();
        }
    }
}
