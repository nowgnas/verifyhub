package com.verifyhub.common.exception;

public class InvalidRequestException extends VerifyhubException {

    public InvalidRequestException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}
