package com.zxuhan.template.service;

import com.zxuhan.template.model.dto.image.ImageData;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;

/**
 * Image service interface.
 * Abstracts image retrieval logic to support multiple sources (e.g., Pexels, Unsplash, AI generation).
 *
 * To add a new image service:
 * 1. Implement this interface
 * 2. Add a corresponding value to ImageMethodEnum
 * 3. Add a configuration class if needed
 */
public interface ImageSearchService {

    /**
     * Get image by request (recommended)
     *
     * @param request image request object containing keywords, prompt, etc.
     * @return image URL, or null on failure
     */
    default String getImage(ImageRequest request) {
        // Default: select the appropriate parameter based on service type
        String param = request.getEffectiveParam(getMethod().isAiGenerated());
        return searchImage(param);
    }

    /**
     * Get image data (for unified upload to COS).
     * Subclasses can override this to return a more efficient format (e.g., byte data).
     *
     * @param request image request object
     * @return ImageData object containing image bytes or URL
     */
    default ImageData getImageData(ImageRequest request) {
        // Default: get URL via getImage, then wrap in ImageData
        String url = getImage(request);
        return ImageData.fromUrl(url);
    }

    /**
     * Search for image by keywords or prompt
     *
     * @param keywords search keywords (for image libraries) or generation prompt (for AI generation)
     * @return image URL, or null on failure
     */
    String searchImage(String keywords);

    /**
     * Get the image service type
     *
     * @return image service type enum
     */
    ImageMethodEnum getMethod();

    /**
     * Get fallback image URL
     *
     * @param position position index (used to generate a unique random image)
     * @return fallback image URL
     */
    String getFallbackImage(int position);

    /**
     * Check whether the service is available.
     * Subclasses can override this for a health check.
     *
     * @return true if the service is available
     */
    default boolean isAvailable() {
        return true;
    }
}
