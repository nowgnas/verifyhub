package com.verifyhub.common.exception;

public class VerifyhubException extends RuntimeException {

    private final ErrorCode errorCode;

    public VerifyhubException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public VerifyhubException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
