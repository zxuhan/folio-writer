package com.zxuhan.template.service;

import com.zxuhan.template.model.entity.User;

/**
 * Quota service interface
 */
public interface QuotaService {

    /**
     * Check whether the user has sufficient quota
     *
     * @param user user
     * @return true if quota is available
     */
    boolean hasQuota(User user);

    /**
     * Consume quota (deduct 1 use)
     *
     * @param user user
     */
    void consumeQuota(User user);

    /**
     * Check and consume quota atomically.
     * Throws an exception if quota is insufficient.
     *
     * @param user user
     */
    void checkAndConsumeQuota(User user);
}
