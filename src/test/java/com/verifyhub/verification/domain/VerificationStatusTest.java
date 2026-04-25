package com.verifyhub.verification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerificationStatusTest {

    @Test
    void identifiesTerminalStatuses() {
        assertThat(VerificationStatus.SUCCESS.isTerminal()).isTrue();
        assertThat(VerificationStatus.FAIL.isTerminal()).isTrue();
        assertThat(VerificationStatus.TIMEOUT.isTerminal()).isTrue();
        assertThat(VerificationStatus.CANCELED.isTerminal()).isTrue();
    }

    @Test
    void identifiesNonTerminalStatuses() {
        assertThat(VerificationStatus.REQUESTED.isTerminal()).isFalse();
        assertThat(VerificationStatus.ROUTED.isTerminal()).isFalse();
        assertThat(VerificationStatus.IN_PROGRESS.isTerminal()).isFalse();
    }
}
