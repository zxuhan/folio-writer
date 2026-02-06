package com.zxuhan.template.constant;

/**
 * User-related constants.
 */
public interface UserConstant {

    /**
     * User login session key.
     */
    String USER_LOGIN_STATE = "user_login";

    //  region Role constants

    /**
     * Default user role.
     */
    String DEFAULT_ROLE = "user";

    /**
     * Admin role.
     */
    String ADMIN_ROLE = "admin";

    /**
     * VIP role.
     */
    String VIP_ROLE = "vip";

    // endregion

    //  region Quota constants

    /**
     * Default quota for regular users.
     */
    int DEFAULT_QUOTA = 5;

    // endregion
}
