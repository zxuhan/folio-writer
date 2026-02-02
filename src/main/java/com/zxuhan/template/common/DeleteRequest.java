package com.zxuhan.template.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Delete request wrapper.
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
