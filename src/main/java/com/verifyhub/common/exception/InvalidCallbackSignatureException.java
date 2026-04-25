package com.verifyhub.common.exception;

public class InvalidCallbackSignatureException extends VerifyhubException {

    public InvalidCallbackSignatureException() {
        super(ErrorCode.INVALID_CALLBACK_SIGNATURE);
    }
}
