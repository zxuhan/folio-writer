package com.zxuhan.template.model.dto.user;

import com.zxuhan.template.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User profile bio
     */
    private String userProfile;

    /**
     * User role: user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}