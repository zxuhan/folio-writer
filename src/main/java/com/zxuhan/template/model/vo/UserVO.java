package com.zxuhan.template.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Desensitized user info
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
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
     * Creation timestamp
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}