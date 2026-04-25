package com.verifyhub.verification.domain;

public enum VerificationStatus {
    REQUESTED(false),
    ROUTED(false),
    IN_PROGRESS(false),
    SUCCESS(true),
    FAIL(true),
    TIMEOUT(true),
    CANCELED(true);

    private final boolean terminal;

    VerificationStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
