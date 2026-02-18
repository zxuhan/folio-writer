package com.zxuhan.template.model.enums;

import lombok.Getter;

/**
 * Image method enum.
 *
 * To add a new image source, add a new enum constant with the correct properties:
 * - isAiGenerated: true if the method generates images via AI (uses prompt), false for library search (uses keywords)
 * - isFallback: true if this method is a fallback option
 */
@Getter
public enum ImageMethodEnum {

    /**
     * Pexels image library search
     */
    PEXELS("PEXELS", "Pexels library", false, false),

    /**
     * Nano Banana AI image generation (Gemini native)
     */
    NANO_BANANA("NANO_BANANA", "Nano Banana AI generation", true, false),

    /**
     * Mermaid diagram generation
     */
    MERMAID("MERMAID", "Mermaid diagram", true, false),

    /**
     * Iconify icon library search
     */
    ICONIFY("ICONIFY", "Iconify icon library", false, false),

    /**
     * Emoji/meme search (Bing image search)
     */
    EMOJI_PACK("EMOJI_PACK", "Emoji pack search", false, false),

    /**
     * SVG concept diagram generation (AI generates SVG code)
     */
    SVG_DIAGRAM("SVG_DIAGRAM", "SVG concept diagram", true, false),

    /**
     * Picsum random image (fallback)
     */
    PICSUM("PICSUM", "Picsum random image", false, true);

    // ============ Extension examples ============
    // DALL_E("DALL_E", "DALL-E AI generation", true, false),
    // MIDJOURNEY("MIDJOURNEY", "Midjourney AI generation", true, false),
    // UNSPLASH("UNSPLASH", "Unsplash library", false, false),
    // STABLE_DIFFUSION("STABLE_DIFFUSION", "Stable Diffusion AI generation", true, false),

    /**
     * Method value
     */
    private final String value;

    /**
     * Method description
     */
    private final String description;

    /**
     * Whether this is an AI generation method.
     * true: uses prompt to generate images (e.g. DALL-E, Midjourney, Nano Banana)
     * false: uses keywords to search a library (e.g. Pexels, Unsplash)
     */
    private final boolean aiGenerated;

    /**
     * Whether this is a fallback method
     */
    private final boolean fallback;

    ImageMethodEnum(String value, String description, boolean aiGenerated, boolean fallback) {
        this.value = value;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.fallback = fallback;
    }

    /**
     * Returns the enum by value.
     *
     * @param value method value
     * @return enum instance, or null if not found
     */
    public static ImageMethodEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ImageMethodEnum methodEnum : values()) {
            if (methodEnum.getValue().equals(value)) {
                return methodEnum;
            }
        }
        return null;
    }

    /**
     * Returns the default library search method.
     */
    public static ImageMethodEnum getDefaultSearchMethod() {
        return PEXELS;
    }

    /**
     * Returns the default AI generation method.
     */
    public static ImageMethodEnum getDefaultAiMethod() {
        return NANO_BANANA;
    }

    /**
     * Returns the fallback method.
     */
    public static ImageMethodEnum getFallbackMethod() {
        return PICSUM;
    }
}
