package com.zxuhan.template.model.enums;

import lombok.Getter;

/**
 * Article phase enum
 */
@Getter
public enum ArticlePhaseEnum {

    PENDING("PENDING", "Pending"),
    TITLE_GENERATING("TITLE_GENERATING", "Generating title"),
    TITLE_SELECTING("TITLE_SELECTING", "Awaiting title selection"),
    OUTLINE_GENERATING("OUTLINE_GENERATING", "Generating outline"),
    OUTLINE_EDITING("OUTLINE_EDITING", "Awaiting outline editing"),
    CONTENT_GENERATING("CONTENT_GENERATING", "Generating content");

    /**
     * Phase value
     */
    private final String value;

    /**
     * Phase description
     */
    private final String description;

    ArticlePhaseEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Returns the enum by value.
     *
     * @param value phase value
     * @return enum instance, or null if not found
     */
    public static ArticlePhaseEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticlePhaseEnum phaseEnum : values()) {
            if (phaseEnum.getValue().equals(value)) {
                return phaseEnum;
            }
        }
        return null;
    }

    /**
     * Returns whether this phase can transition to the target phase.
     *
     * @param targetPhase target phase
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(ArticlePhaseEnum targetPhase) {
        if (targetPhase == null) {
            return false;
        }

        // Define valid state transitions
        return switch (this) {
            case PENDING -> targetPhase == TITLE_GENERATING;
            case TITLE_GENERATING -> targetPhase == TITLE_SELECTING;
            case TITLE_SELECTING -> targetPhase == OUTLINE_GENERATING;
            case OUTLINE_GENERATING -> targetPhase == OUTLINE_EDITING;
            case OUTLINE_EDITING -> targetPhase == CONTENT_GENERATING;
            case CONTENT_GENERATING -> false; // Terminal phase; no further transitions
        };
    }
}
