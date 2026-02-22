package com.zxuhan.template.model.enums;

import lombok.Getter;

/**
 * Payment status enum
 */
@Getter
public enum PaymentStatusEnum {

    PENDING("PENDING", "Pending"),
    SUCCEEDED("SUCCEEDED", "Payment succeeded"),
    FAILED("FAILED", "Payment failed"),
    REFUNDED("REFUNDED", "Refunded");

    private final String value;
    private final String description;

    PaymentStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static PaymentStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentStatusEnum statusEnum : values()) {
            if (statusEnum.getValue().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
