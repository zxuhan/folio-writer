package com.zxuhan.template.model.enums;

import lombok.Getter;

/**
 * SSE message type enum
 */
@Getter
public enum SseMessageTypeEnum {

    /**
     * Agent 1 complete (title options generated)
     */
    AGENT1_COMPLETE("AGENT1_COMPLETE", "Title options generated"),

    /**
     * Title options ready (awaiting user selection)
     */
    TITLES_GENERATED("TITLES_GENERATED", "Titles generated"),

    /**
     * Agent 2 streaming (outline)
     */
    AGENT2_STREAMING("AGENT2_STREAMING", "Outline streaming"),

    /**
     * Agent 2 complete (outline generated)
     */
    AGENT2_COMPLETE("AGENT2_COMPLETE", "Outline generated"),

    /**
     * Outline ready (awaiting user editing)
     */
    OUTLINE_GENERATED("OUTLINE_GENERATED", "Outline ready"),

    /**
     * Agent 3 streaming (content)
     */
    AGENT3_STREAMING("AGENT3_STREAMING", "Content streaming"),

    /**
     * Agent 3 complete (content generated)
     */
    AGENT3_COMPLETE("AGENT3_COMPLETE", "Content generated"),

    /**
     * Agent 4 complete (image requirements analyzed)
     */
    AGENT4_COMPLETE("AGENT4_COMPLETE", "Image requirements analyzed"),

    /**
     * Single image complete
     */
    IMAGE_COMPLETE("IMAGE_COMPLETE", "Image complete"),

    /**
     * Agent 5 complete (images generated)
     */
    AGENT5_COMPLETE("AGENT5_COMPLETE", "Images generated"),

    /**
     * Image-text synthesis complete
     */
    MERGE_COMPLETE("MERGE_COMPLETE", "Merge complete"),

    /**
     * All steps complete
     */
    ALL_COMPLETE("ALL_COMPLETE", "All complete"),

    /**
     * Error
     */
    ERROR("ERROR", "Error");

    /**
     * Message type value
     */
    private final String value;

    /**
     * Message type description
     */
    private final String description;

    SseMessageTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Returns the streaming message prefix (with colon), e.g. "AGENT2_STREAMING:".
     *
     * @return message prefix including trailing colon
     */
    public String getStreamingPrefix() {
        return this.value + ":";
    }
}
