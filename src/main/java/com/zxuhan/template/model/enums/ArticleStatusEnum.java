package com.zxuhan.template.model.enums;

import lombok.Getter;

/**
 * Article status enum
 */
@Getter
public enum ArticleStatusEnum {

    PENDING("PENDING", "Pending"),
    PROCESSING("PROCESSING", "Processing"),
    COMPLETED("COMPLETED", "Completed"),
    FAILED("FAILED", "Failed");

    /**
     * Status value
     */
    private final String value;

    /**
     * Status description
     */
    private final String description;

    ArticleStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Returns the enum by value.
     *
     * @param value status value
     * @return enum instance, or null if not found
     */
    public static ArticleStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticleStatusEnum statusEnum : values()) {
            if (statusEnum.getValue().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
