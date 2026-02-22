package com.zxuhan.template.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment record entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "payment_record", camelToUnderline = false)
public class PaymentRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Stripe Checkout Session ID
     */
    private String stripeSessionId;

    /**
     * Stripe payment intent ID
     */
    private String stripePaymentIntentId;

    /**
     * Amount (USD)
     */
    private BigDecimal amount;

    /**
     * Currency
     */
    private String currency;

    /**
     * Status: PENDING/SUCCEEDED/FAILED/REFUNDED
     */
    private String status;

    /**
     * Product type: VIP_PERMANENT
     */
    private String productType;

    /**
     * Description
     */
    private String description;

    /**
     * Refund timestamp
     */
    private LocalDateTime refundTime;

    /**
     * Refund reason
     */
    private String refundReason;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

    /**
     * Updated timestamp
     */
    private LocalDateTime updateTime;
}
