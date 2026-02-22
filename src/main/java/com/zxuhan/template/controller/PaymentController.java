package com.zxuhan.template.controller;

import com.zxuhan.template.annotation.AuthCheck;
import com.zxuhan.template.common.BaseResponse;
import com.zxuhan.template.common.ResultUtils;
import com.zxuhan.template.constant.UserConstant;
import com.zxuhan.template.exception.BusinessException;
import com.zxuhan.template.exception.ErrorCode;
import com.zxuhan.template.model.entity.PaymentRecord;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.service.PaymentService;
import com.zxuhan.template.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment controller
 *
 */
@RestController
@RequestMapping("/payment")
@Slf4j
@Tag(name = "PaymentController", description = "Payment API")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private UserService userService;

    /**
     * Create VIP payment session
     */
    @PostMapping("/create-vip-session")
    @Operation(summary = "Create VIP payment session")
    public BaseResponse<String> createVipPaymentSession(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        try {
            String sessionUrl = paymentService.createVipPaymentSession(loginUser.getId());
            return ResultUtils.success(sessionUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create payment session", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to create payment session");
        }
    }

    /**
     * Request a refund
     */
    @PostMapping("/refund")
    @Operation(summary = "Request a refund")
    @AuthCheck(mustRole = UserConstant.VIP_ROLE)
    public BaseResponse<Boolean> refund(
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        try {
            boolean success = paymentService.handleRefund(loginUser.getId(), reason);
            return ResultUtils.success(success);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refund failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Refund failed");
        }
    }

    /**
     * Get payment records for the current user
     */
    @GetMapping("/records")
    @Operation(summary = "List payment records for the current user")
    public BaseResponse<List<PaymentRecord>> getPaymentRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<PaymentRecord> records = paymentService.getPaymentRecords(loginUser.getId());
        return ResultUtils.success(records);
    }
}
