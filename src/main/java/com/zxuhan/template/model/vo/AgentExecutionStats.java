package com.zxuhan.template.model.vo;

import com.zxuhan.template.model.entity.AgentLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Agent execution statistics VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Total duration in milliseconds
     */
    private Integer totalDurationMs;

    /**
     * Number of agents
     */
    private Integer agentCount;

    /**
     * Per-agent durations (key: agentName, value: durationMs)
     */
    private Map<String, Integer> agentDurations;

    /**
     * Overall status: SUCCESS (all succeeded), FAILED (at least one failed), RUNNING (in progress)
     */
    private String overallStatus;

    /**
     * Detailed log entries
     */
    private List<AgentLog> logs;
}
