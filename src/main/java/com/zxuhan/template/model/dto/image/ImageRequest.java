package com.zxuhan.template.model.dto.image;

import lombok.Builder;
import lombok.Data;

/**
 * Image request object; unified wrapper for all image retrieval parameters.
 */
@Data
@Builder
public class ImageRequest {

    /**
     * Search keywords (for library search)
     */
    private String keywords;

    /**
     * Generation prompt (for AI image generation)
     */
    private String prompt;

    /**
     * Image position index
     */
    private Integer position;

    /**
     * Image type (cover/section)
     */
    private String type;

    /**
     * Aspect ratio (e.g. 16:9, 1:1)
     */
    private String aspectRatio;

    /**
     * Image style description
     */
    private String style;

    /**
     * Returns the effective parameter for search or generation.
     * AI generation prefers prompt; library search prefers keywords.
     *
     * @param isAiGenerated whether this is an AI generation request
     * @return effective parameter string
     */
    public String getEffectiveParam(boolean isAiGenerated) {
        if (isAiGenerated) {
            return prompt != null && !prompt.isEmpty() ? prompt : keywords;
        }
        return keywords != null && !keywords.isEmpty() ? keywords : prompt;
    }
}
