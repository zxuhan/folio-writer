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
 * Agent execution log entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "agent_log", camelToUnderline = false)
public class AgentLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Agent name
     */
    private String agentName;

    /**
     * Start time
     */
    private LocalDateTime startTime;

    /**
     * End time
     */
    private LocalDateTime endTime;

    /**
     * Duration in milliseconds
     */
    private Integer durationMs;

    /**
     * Status: SUCCESS/FAILED
     */
    private String status;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Prompt used
     */
    private String prompt;

    /**
     * Input data (JSON)
     */
    private String inputData;

    /**
     * Output data (JSON)
     */
    private String outputData;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

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
