package com.verifyhub.common.exception;

public class ProviderUnavailableException extends VerifyhubException {

    public ProviderUnavailableException() {
        super(ErrorCode.PROVIDER_UNAVAILABLE);
    }
}
