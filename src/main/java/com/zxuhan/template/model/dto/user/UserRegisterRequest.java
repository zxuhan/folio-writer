package com.zxuhan.template.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User registration request
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User password
     */
    private String userPassword;

    /**
     * Confirm password
     */
    private String checkPassword;
}