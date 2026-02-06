package com.zxuhan.template.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User creation request
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * User nickname
     */
    private String userName;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User avatar URL
     */
    private String userAvatar;

    /**
     * User profile bio
     */
    private String userProfile;

    /**
     * User role: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}