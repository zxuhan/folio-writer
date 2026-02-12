package com.zxuhan.template.aop;

import com.zxuhan.template.annotation.AgentExecution;
import com.zxuhan.template.model.dto.article.ArticleState;
import com.zxuhan.template.model.entity.AgentLog;
import com.zxuhan.template.service.AgentLogService;
import com.zxuhan.template.utils.GsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent execution AOP aspect.
 * Automatically records agent execution logs and performance metrics.
 */
@Aspect
@Component
@Slf4j
public class AgentExecutionAspect {

    @Resource
    private AgentLogService agentLogService;

    @Around("@annotation(agentExecution)")
    public Object aroundAgentExecution(ProceedingJoinPoint pjp, AgentExecution agentExecution) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime startDateTime = LocalDateTime.now();

        // Extract taskId and input data
        String taskId = extractTaskId(pjp);
        String inputData = extractInputData(pjp);
        String prompt = extractPrompt(pjp);

        // Create log object
        AgentLog agentLog = AgentLog.builder()
                .taskId(taskId)
                .agentName(agentExecution.value())
                .startTime(startDateTime)
                .status("RUNNING")
                .prompt(prompt)
                .inputData(inputData)
                .build();

        Object result = null;
        try {
            // Execute target method
            result = pjp.proceed();

            // Record success status
            agentLog.setStatus("SUCCESS");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setOutputData(extractOutputData(result));

            log.info("Agent executed successfully: {}, taskId={}, durationMs={}",
                    agentExecution.value(), taskId, agentLog.getDurationMs());

        } catch (Throwable e) {
            // Record failure status
            agentLog.setStatus("FAILED");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getName());

            log.error("Agent execution failed: {}, taskId={}, error={}",
                    agentExecution.value(), taskId, e.getMessage(), e);

            throw e;
        } finally {
            // Save log asynchronously
            agentLogService.saveLogAsync(agentLog);
        }

        return result;
    }

    /**
     * Extract taskId from method arguments.
     */
    private String extractTaskId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "unknown";
        }

        // Prefer extraction from ArticleState
        for (Object arg : args) {
            if (arg instanceof ArticleState) {
                return ((ArticleState) arg).getTaskId();
            }
        }

        // Fallback: try the first String argument (may be taskId)
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }

        return "unknown";
    }

    /**
     * Extract input data (simplified — only key fields are recorded).
     */
    private String extractInputData(ProceedingJoinPoint pjp) {
        try {
            Object[] args = pjp.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }

            Map<String, Object> inputMap = new HashMap<>();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                Object arg = args[i];
                // Only record primitive types and simple objects to avoid large payloads
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    inputMap.put(paramNames[i], arg);
                } else if (arg instanceof ArticleState) {
                    ArticleState state = (ArticleState) arg;
                    inputMap.put("taskId", state.getTaskId());
                    if (state.getTitle() != null) {
                        inputMap.put("mainTitle", state.getTitle().getMainTitle());
                    }
                }
            }

            return inputMap.isEmpty() ? null : GsonUtils.toJson(inputMap);
        } catch (Exception e) {
            log.warn("Failed to extract input data", e);
            return null;
        }
    }

    /**
     * Extract output data (simplified).
     */
    private String extractOutputData(Object result) {
        try {
            if (result == null) {
                return null;
            }

            // Only record primitive types to avoid large payloads
            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return String.valueOf(result);
            }

            // For collections, record only the size
            if (result instanceof java.util.List) {
                return "{\"listSize\": " + ((java.util.List<?>) result).size() + "}";
            }

            return "{\"type\": \"" + result.getClass().getSimpleName() + "\"}";
        } catch (Exception e) {
            log.warn("Failed to extract output data", e);
            return null;
        }
    }

    /**
     * Extract the prompt used (inferred from method name or arguments).
     */
    private String extractPrompt(ProceedingJoinPoint pjp) {
        try {
            // Infer prompt from method name, or extract from arguments — simplified here
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        } catch (Exception e) {
            return null;
        }
    }
}
