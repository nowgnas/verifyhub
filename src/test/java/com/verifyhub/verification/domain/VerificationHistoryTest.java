package com.verifyhub.verification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class VerificationHistoryTest {

    @Test
    void recordsStatusTransitionHistory() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 25, 12, 1);

        VerificationHistory history = VerificationHistory.of(
                "verif_123",
                null,
                VerificationStatus.REQUESTED,
                VerificationEvent.VERIFICATION_REQUESTED,
                "verification requested",
                null,
                null,
                createdAt
        );

        assertThat(history.verificationId()).isEqualTo("verif_123");
        assertThat(history.fromStatus()).isNull();
        assertThat(history.toStatus()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(history.eventType()).isEqualTo(VerificationEvent.VERIFICATION_REQUESTED);
        assertThat(history.reason()).isEqualTo("verification requested");
        assertThat(history.createdAt()).isEqualTo(createdAt);
    }
}
