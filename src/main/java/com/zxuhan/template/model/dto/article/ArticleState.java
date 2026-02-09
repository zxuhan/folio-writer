package com.zxuhan.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Article generation state (shared across agents)
 */
@Data
public class ArticleState implements Serializable {

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Article topic
     */
    private String topic;

    /**
     * Supplementary description from the user
     */
    private String userDescription;

    /**
     * Article style
     */
    private String style;

    /**
     * Current phase
     */
    private String phase;

    /**
     * Title options list (output of agent 1)
     */
    private List<TitleOption> titleOptions;

    /**
     * Selected title result (output of agent 1)
     */
    private TitleResult title;

    /**
     * Outline result (output of agent 2)
     */
    private OutlineResult outline;

    /**
     * Article content (output of agent 3)
     */
    private String content;

    /**
     * Image requirements list (output of agent 4)
     */
    private List<ImageRequirement> imageRequirements;

    /**
     * Cover image URL (also stored as position=1 in the images list)
     */
    private String coverImage;

    /**
     * Image results list (output of agent 5)
     */
    private List<ImageResult> images;

    /**
     * Allowed image methods (empty means all methods are permitted)
     */
    private List<String> enabledImageMethods;

    /**
     * Title option
     */
    @Data
    public static class TitleOption implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    /**
     * Title result
     */
    @Data
    public static class TitleResult implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    /**
     * Outline result
     */
    @Data
    public static class OutlineResult implements Serializable {
        private List<OutlineSection> sections;
    }

    /**
     * Outline section
     */
    @Data
    public static class OutlineSection implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    /**
     * Image requirement
     */
    @Data
    public static class ImageRequirement implements Serializable {
        private Integer position;
        private String type;
        private String sectionTitle;
        private String keywords;
        /**
         * Image source: PEXELS (library search) or NANO_BANANA (AI generation)
         */
        private String imageSource;
        /**
         * AI generation prompt (used when imageSource is NANO_BANANA)
         */
        private String prompt;
        /**
         * Placeholder ID for locating the insertion point in the content, format: {{IMAGE_PLACEHOLDER_N}}
         */
        private String placeholderId;
    }

    /**
     * Image result
     */
    @Data
    public static class ImageResult implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
        /**
         * Placeholder ID for locating the insertion point in the content
         */
        private String placeholderId;
    }

    /**
     * Agent 4 return value (content with placeholders and image requirements list)
     */
    @Data
    public static class Agent4Result implements Serializable {
        /**
         * Content with image placeholders
         */
        private String contentWithPlaceholders;
        /**
         * Image requirements list
         */
        private List<ImageRequirement> imageRequirements;
    }

    /**
     * Full article content with images (after synthesis)
     */
    private String fullContent;

    private static final long serialVersionUID = 1L;
}
