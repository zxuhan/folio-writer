package com.zxuhan.template.common;

import com.zxuhan.template.exception.ErrorCode;

/**
 * Utility class for building API responses.
 */
public class ResultUtils {

    /**
     * Success response.
     *
     * @param data the response data
     * @param <T>  data type
     * @return response
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Error response.
     *
     * @param errorCode the error code
     * @return response
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Error response.
     *
     * @param code    the error code value
     * @param message the error message
     * @return response
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * Error response.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @return response
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
