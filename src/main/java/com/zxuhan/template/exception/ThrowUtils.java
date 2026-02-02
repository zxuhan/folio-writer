package com.zxuhan.template.exception;

/**
 * Utility class for conditional exception throwing
 *
 */
public class ThrowUtils {

    /**
     * Throws the given exception if the condition is true
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * Throws a BusinessException if the condition is true
     *
     * @param condition condition to evaluate
     * @param errorCode error code to use
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * Throws a BusinessException with a custom message if the condition is true
     *
     * @param condition condition to evaluate
     * @param errorCode error code to use
     * @param message   error message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
