package com.zxuhan.template.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Desensitized login user info
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * User ID
     */
    private Long id;

    /**
     * User account
     */
    private String userAccount;

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

    /**
     * Remaining quota
     */
    private Integer quota;

    /**
     * VIP activation timestamp
     */
    private LocalDateTime vipTime;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

    /**
     * Updated timestamp
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}