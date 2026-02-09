package com.zxuhan.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Create-article request
 */
@Data
public class ArticleCreateRequest implements Serializable {

    /**
     * Article topic
     */
    private String topic;

    /**
     * Article style: tech/emotional/educational/humorous; nullable
     */
    private String style;

    /**
     * Allowed image methods (null or empty means all methods are permitted)
     * Possible values: PEXELS, NANO_BANANA, MERMAID, ICONIFY, EMOJI_PACK, SVG_DIAGRAM
     */
    private List<String> enabledImageMethods;

    private static final long serialVersionUID = 1L;
}
