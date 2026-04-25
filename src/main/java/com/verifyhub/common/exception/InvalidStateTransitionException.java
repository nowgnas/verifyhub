package com.verifyhub.common.exception;

public class InvalidStateTransitionException extends VerifyhubException {

    public InvalidStateTransitionException() {
        super(ErrorCode.INVALID_STATE_TRANSITION);
    }

    public InvalidStateTransitionException(String message) {
        super(ErrorCode.INVALID_STATE_TRANSITION, message);
    }
}
