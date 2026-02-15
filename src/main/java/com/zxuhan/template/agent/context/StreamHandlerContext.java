package com.zxuhan.template.agent.context;

import java.util.function.Consumer;

/**
 * Streaming output handler context.
 * Uses ThreadLocal to store the streamHandler, avoiding placing it in StateGraph state (not serializable).
 */
public class StreamHandlerContext {

    private static final ThreadLocal<Consumer<String>> STREAM_HANDLER = new ThreadLocal<>();

    /**
     * Set the streaming output handler.
     *
     * @param handler the handler
     */
    public static void set(Consumer<String> handler) {
        STREAM_HANDLER.set(handler);
    }

    /**
     * Get the streaming output handler.
     *
     * @return the handler, may be null
     */
    public static Consumer<String> get() {
        return STREAM_HANDLER.get();
    }

    /**
     * Clear the context.
     * Must be called after use to prevent memory leaks.
     */
    public static void clear() {
        STREAM_HANDLER.remove();
    }

    /**
     * Send a message to the streaming output.
     * Silently ignored if no handler is set.
     *
     * @param message the message content
     */
    public static void send(String message) {
        Consumer<String> handler = STREAM_HANDLER.get();
        if (handler != null && message != null) {
            handler.accept(message);
        }
    }
}
