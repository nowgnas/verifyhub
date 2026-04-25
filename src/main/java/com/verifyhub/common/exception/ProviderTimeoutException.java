package com.verifyhub.common.exception;

public class ProviderTimeoutException extends VerifyhubException {

    public ProviderTimeoutException() {
        super(ErrorCode.PROVIDER_TIMEOUT);
    }
}
