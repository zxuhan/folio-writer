package com.zxuhan.template.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Article style enum
 */
@Getter
public enum ArticleStyleEnum {

    TECH("tech", "Tech style"),
    EMOTIONAL("emotional", "Emotional style"),
    EDUCATIONAL("educational", "Educational style"),
    HUMOROUS("humorous", "Humorous style");

    private final String value;
    private final String text;

    ArticleStyleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * Returns all style values.
     */
    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(ArticleStyleEnum::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Returns the enum by value.
     */
    public static ArticleStyleEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (ArticleStyleEnum styleEnum : ArticleStyleEnum.values()) {
            if (styleEnum.getValue().equals(value)) {
                return styleEnum;
            }
        }
        return null;
    }

    /**
     * Returns whether the given value is a valid style.
     */
    public static boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return true; // null/empty is allowed
        }
        return getEnumByValue(value) != null;
    }
}
