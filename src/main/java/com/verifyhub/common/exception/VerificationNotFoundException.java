package com.verifyhub.common.exception;

public class VerificationNotFoundException extends VerifyhubException {

    public VerificationNotFoundException() {
        super(ErrorCode.VERIFICATION_NOT_FOUND);
    }

    public VerificationNotFoundException(String verificationId) {
        super(ErrorCode.VERIFICATION_NOT_FOUND, "Verification request was not found: " + verificationId);
    }
}
