package com.zxuhan.template.common;

import lombok.Data;

/**
 * Pagination request wrapper.
 */
@Data
public class PageRequest {

    /**
     * Current page number.
     */
    private int pageNum = 1;

    /**
     * Page size.
     */
    private int pageSize = 10;

    /**
     * Sort field.
     */
    private String sortField;

    /**
     * Sort order (default: descending).
     */
    private String sortOrder = "descend";
}
