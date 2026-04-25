package com.verifyhub.common.response;

import com.verifyhub.common.exception.ErrorCode;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final String traceId;

    private ErrorResponse(String code, String message, String traceId) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String traceId) {
        return new ErrorResponse(errorCode.getCode(), message, traceId);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }
}
