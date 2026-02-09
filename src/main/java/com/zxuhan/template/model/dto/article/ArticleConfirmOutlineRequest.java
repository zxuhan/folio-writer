package com.zxuhan.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Confirm-outline request
 */
@Data
public class ArticleConfirmOutlineRequest implements Serializable {

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Outline edited by the user
     */
    private List<ArticleState.OutlineSection> outline;

    private static final long serialVersionUID = 1L;
}
