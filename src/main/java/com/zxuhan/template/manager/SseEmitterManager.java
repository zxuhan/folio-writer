package com.zxuhan.template.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.zxuhan.template.constant.ArticleConstant.SSE_RECONNECT_TIME_MS;
import static com.zxuhan.template.constant.ArticleConstant.SSE_TIMEOUT_MS;

/**
 * SSE Emitter manager
 *
 */
@Component
@Slf4j
public class SseEmitterManager {

    /**
     * Registry of all active SseEmitters
     */
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * Create an SseEmitter
     *
     * @param taskId task ID
     * @return SseEmitter
     */
    public SseEmitter createEmitter(String taskId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // Timeout callback
        emitter.onTimeout(() -> {
            log.warn("SSE connection timed out, taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        // Completion callback
        emitter.onCompletion(() -> {
            log.info("SSE connection completed, taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        // Error callback
        emitter.onError((e) -> {
            log.error("SSE connection error, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        });

        emitterMap.put(taskId, emitter);
        log.info("SSE connection created, taskId={}", taskId);

        return emitter;
    }

    /**
     * Send a message
     *
     * @param taskId  task ID
     * @param message message content
     */
    public void send(String taskId, String message) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE Emitter not found, taskId={}", taskId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME_MS));
            log.debug("SSE message sent successfully, taskId={}, message={}", taskId, message);
        } catch (IOException e) {
            log.error("Failed to send SSE message, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        }
    }

    /**
     * Complete the connection
     *
     * @param taskId task ID
     */
    public void complete(String taskId) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE Emitter not found, taskId={}", taskId);
            return;
        }

        try {
            emitter.complete();
            log.info("SSE connection completed, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Failed to complete SSE connection, taskId={}", taskId, e);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * Check whether an emitter exists
     *
     * @param taskId task ID
     * @return true if the emitter exists
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}
