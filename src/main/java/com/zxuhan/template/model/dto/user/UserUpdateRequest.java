package com.zxuhan.template.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User update request
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

    /**
     * User avatar URL
     */
    private String userAvatar;

    /**
     * User profile bio
     */
    private String userProfile;

    /**
     * User role: user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}