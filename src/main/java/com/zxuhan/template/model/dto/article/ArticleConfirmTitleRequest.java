package com.zxuhan.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;

/**
 * Confirm-title request
 */
@Data
public class ArticleConfirmTitleRequest implements Serializable {

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Selected main title
     */
    private String selectedMainTitle;

    /**
     * Selected subtitle
     */
    private String selectedSubTitle;

    /**
     * Optional supplementary description from the user
     */
    private String userDescription;

    private static final long serialVersionUID = 1L;
}
