package com.verifyhub.common.exception;

public class RetryNotAllowedException extends VerifyhubException {

    public RetryNotAllowedException() {
        super(ErrorCode.RETRY_NOT_ALLOWED);
    }
}
