package com.zxuhan.template.controller;

import com.zxuhan.template.annotation.AuthCheck;
import com.zxuhan.template.common.BaseResponse;
import com.zxuhan.template.common.ResultUtils;
import com.zxuhan.template.constant.UserConstant;
import com.zxuhan.template.model.vo.StatisticsVO;
import com.zxuhan.template.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Statistics controller
 *
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
@Tag(name = "StatisticsController", description = "Statistics API")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * Get system statistics (admin only)
     */
    @GetMapping("/overview")
    @Operation(summary = "Get system statistics")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<StatisticsVO> getStatistics() {
        StatisticsVO statistics = statisticsService.getStatistics();
        return ResultUtils.success(statistics);
    }
}
