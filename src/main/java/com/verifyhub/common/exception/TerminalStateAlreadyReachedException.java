package com.verifyhub.common.exception;

public class TerminalStateAlreadyReachedException extends VerifyhubException {

    public TerminalStateAlreadyReachedException() {
        super(ErrorCode.TERMINAL_STATE_ALREADY_REACHED);
    }
}
