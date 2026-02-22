package com.zxuhan.template.service.impl;

import com.zxuhan.template.exception.BusinessException;
import com.zxuhan.template.exception.ErrorCode;
import com.zxuhan.template.mapper.UserMapper;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.service.QuotaService;
import com.zxuhan.template.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zxuhan.template.constant.UserConstant.ADMIN_ROLE;
import static com.zxuhan.template.constant.UserConstant.VIP_ROLE;

/**
 * Quota service implementation.
 *
 * Concurrency safety notes:
 * 1. Uses atomic database update (UPDATE ... SET quota = quota - 1 WHERE quota > 0) to avoid race conditions
 * 2. Determines success by affected row count, eliminating the need for a separate read-then-write
 * 3. Uses @Transactional to ensure consistency between quota deduction and subsequent operations
 */
@Service
@Slf4j
public class QuotaServiceImpl implements QuotaService {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean hasQuota(User user) {
        // Admins and VIP users have unlimited quota
        if (isAdmin(user) || isVip(user)) {
            return true;
        }
        // Query latest quota from database to avoid stale cached data
        User freshUser = userService.getById(user.getId());
        if (freshUser == null) {
            return false;
        }
        Integer quota = freshUser.getQuota();
        return quota != null && quota > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(User user) {
        // Admins and VIP users do not consume quota
        if (isAdmin(user) || isVip(user)) {
            return;
        }

        // Atomic update: UPDATE user SET quota = quota - 1 WHERE id = ? AND quota > 0
        // Determine success by affected rows to avoid concurrency issues
        int affectedRows = userMapper.decrementQuota(user.getId());

        if (affectedRows > 0) {
            log.info("User quota consumed, userId={}", user.getId());
        } else {
            log.warn("User quota deduction failed (insufficient quota or concurrent conflict), userId={}", user.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndConsumeQuota(User user) {
        // Skip check for admins and VIP users
        if (isAdmin(user) || isVip(user)) {
            return;
        }

        // Atomic update: check and consume merged into one atomic operation
        // UPDATE user SET quota = quota - 1 WHERE id = ? AND quota > 0
        int affectedRows = userMapper.decrementQuota(user.getId());

        if (affectedRows == 0) {
            // Zero affected rows means quota is insufficient (consumed by another request)
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Insufficient quota, cannot create article");
        }

        log.info("User quota checked and consumed, userId={}", user.getId());
    }

    /**
     * Check whether the user is an admin
     */
    private boolean isAdmin(User user) {
        return ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * Check whether the user is VIP
     */
    private boolean isVip(User user) {
        return VIP_ROLE.equals(user.getUserRole());
    }
}
