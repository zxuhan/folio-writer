package com.zxuhan.template.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "user", camelToUnderline = false)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User password
     */
    private String userPassword;

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
     * Last edit timestamp
     */
    private LocalDateTime editTime;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

    /**
     * Updated timestamp
     */
    private LocalDateTime updateTime;

    /**
     * Logical delete flag
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}
