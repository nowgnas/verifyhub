package com.verifyhub.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VERIFICATION_NOT_FOUND("VERIFICATION_NOT_FOUND", "Verification request was not found", HttpStatus.NOT_FOUND),
    DUPLICATE_VERIFICATION_REQUEST("DUPLICATE_VERIFICATION_REQUEST", "Duplicate verification request", HttpStatus.CONFLICT),
    INVALID_STATE_TRANSITION("INVALID_STATE_TRANSITION", "Invalid verification state transition", HttpStatus.CONFLICT),
    TERMINAL_STATE_ALREADY_REACHED("TERMINAL_STATE_ALREADY_REACHED", "Terminal state has already been reached", HttpStatus.CONFLICT),
    PROVIDER_UNAVAILABLE("PROVIDER_UNAVAILABLE", "No available verification provider", HttpStatus.SERVICE_UNAVAILABLE),
    PROVIDER_CALL_FAILED("PROVIDER_CALL_FAILED", "Verification provider call failed", HttpStatus.BAD_GATEWAY),
    PROVIDER_TIMEOUT("PROVIDER_TIMEOUT", "Verification provider timed out", HttpStatus.GATEWAY_TIMEOUT),
    INVALID_CALLBACK_SIGNATURE("INVALID_CALLBACK_SIGNATURE", "Invalid provider callback signature", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
