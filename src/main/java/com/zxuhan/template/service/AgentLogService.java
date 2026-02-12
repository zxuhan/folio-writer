package com.zxuhan.template.service;

import com.mybatisflex.core.service.IService;
import com.zxuhan.template.model.entity.AgentLog;
import com.zxuhan.template.model.vo.AgentExecutionStats;

import java.util.List;

/**
 * Agent log service
 */
public interface AgentLogService extends IService<AgentLog> {

    /**
     * Save log asynchronously
     *
     * @param log log object
     */
    void saveLogAsync(AgentLog log);

    /**
     * Get all logs by task ID
     *
     * @param taskId task ID
     * @return list of logs
     */
    List<AgentLog> getLogsByTaskId(String taskId);

    /**
     * Get task execution statistics
     *
     * @param taskId task ID
     * @return execution statistics
     */
    AgentExecutionStats getExecutionStats(String taskId);
}
