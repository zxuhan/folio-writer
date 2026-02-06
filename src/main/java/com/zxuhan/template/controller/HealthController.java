package com.zxuhan.template.controller;

import com.zxuhan.template.common.BaseResponse;
import com.zxuhan.template.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller
 * Used for Docker container health checks and load balancer probes
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Health check endpoint (supports trailing slash and no trailing slash)
     */
    @GetMapping({"", "/"})
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}
