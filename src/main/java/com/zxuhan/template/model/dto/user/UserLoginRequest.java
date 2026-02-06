package com.zxuhan.template.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User login request
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User password
     */
    private String userPassword;
}