package com.verifyhub.common.exception;

public class ProviderCallFailedException extends VerifyhubException {

    public ProviderCallFailedException() {
        super(ErrorCode.PROVIDER_CALL_FAILED);
    }

    public ProviderCallFailedException(String message) {
        super(ErrorCode.PROVIDER_CALL_FAILED, message);
    }
}
