package com.verifyhub.common.exception;

public class DuplicateVerificationRequestException extends VerifyhubException {

    public DuplicateVerificationRequestException() {
        super(ErrorCode.DUPLICATE_VERIFICATION_REQUEST);
    }
}
