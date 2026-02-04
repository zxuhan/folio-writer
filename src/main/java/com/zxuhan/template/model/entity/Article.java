package com.zxuhan.template.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Article entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "article", camelToUnderline = false)
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * Task ID (UUID)
     */
    private String taskId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Article topic
     */
    private String topic;

    /**
     * Supplementary description from the user
     */
    private String userDescription;

    /**
     * Allowed image methods list (JSON)
     */
    private String enabledImageMethods;

    /**
     * Article style: tech/emotional/educational/humorous; nullable
     */
    private String style;

    /**
     * Main title
     */
    private String mainTitle;

    /**
     * Subtitle
     */
    private String subTitle;

    /**
     * Title options list (JSON)
     */
    private String titleOptions;

    /**
     * Outline (JSON)
     */
    private String outline;

    /**
     * Article content (Markdown, without images)
     */
    private String content;

    /**
     * Full article content (Markdown, with images)
     */
    private String fullContent;

    /**
     * Cover image URL
     */
    private String coverImage;

    /**
     * Image list (JSON array; position=1 is the cover image)
     */
    private String images;

    /**
     * Status: PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * Current phase: PENDING/TITLE_GENERATING/TITLE_SELECTING/OUTLINE_GENERATING/OUTLINE_EDITING/CONTENT_GENERATING
     */
    private String phase;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

    /**
     * Completion timestamp
     */
    private LocalDateTime completedTime;

    /**
     * Updated timestamp
     */
    private LocalDateTime updateTime;

    /**
     * Logical delete flag
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}
