package com.zxuhan.template.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Statistics VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Articles created today
     */
    private Long todayCount;

    /**
     * Articles created this week
     */
    private Long weekCount;

    /**
     * Articles created this month
     */
    private Long monthCount;

    /**
     * Total articles created
     */
    private Long totalCount;

    /**
     * Success rate (percentage)
     */
    private Double successRate;

    /**
     * Average duration in milliseconds
     */
    private Integer avgDurationMs;

    /**
     * Active user count (this week)
     */
    private Long activeUserCount;

    /**
     * Total user count
     */
    private Long totalUserCount;

    /**
     * VIP user count
     */
    private Long vipUserCount;

    /**
     * Total quota consumed
     */
    private Long quotaUsed;
}
