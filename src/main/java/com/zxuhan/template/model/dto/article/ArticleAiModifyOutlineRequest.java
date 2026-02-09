package com.zxuhan.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;

/**
 * AI-modify-outline request
 */
@Data
public class ArticleAiModifyOutlineRequest implements Serializable {

    /**
     * Task ID
     */
    private String taskId;

    /**
     * User modification suggestion
     */
    private String modifySuggestion;

    private static final long serialVersionUID = 1L;
}
