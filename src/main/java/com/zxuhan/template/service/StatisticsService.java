package com.zxuhan.template.service;

import com.zxuhan.template.model.vo.StatisticsVO;

/**
 * Statistics service
 */
public interface StatisticsService {

    /**
     * Get system statistics
     *
     * @return statistics data
     */
    StatisticsVO getStatistics();
}
