package com.zxuhan.template.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zxuhan.template.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * User mapper
 *
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * Atomically decrement the user's quota.
     * The quota > 0 condition ensures concurrency safety and prevents over-deduction.
     *
     * @param userId user ID
     * @return number of rows affected; 1 means success, 0 means insufficient quota
     */
    @Update("UPDATE user SET quota = quota - 1 WHERE id = #{userId} AND quota > 0")
    int decrementQuota(@Param("userId") Long userId);

}
