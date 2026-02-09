package com.zxuhan.template.model.dto.article;

import com.zxuhan.template.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Query-article request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {

    /**
     * User ID
     */
    private Long userId;

    /**
     * Article status
     */
    private String status;

    private static final long serialVersionUID = 1L;
}
