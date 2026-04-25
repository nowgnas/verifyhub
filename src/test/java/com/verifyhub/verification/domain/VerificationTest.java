package com.verifyhub.verification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class VerificationTest {

    @Test
    void createsRequestedVerificationWithoutPersonalInformation() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 4, 25, 12, 0);

        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                requestedAt
        );

        assertThat(verification.getVerificationId()).isEqualTo("verif_123");
        assertThat(verification.getUserId()).isEqualTo("user-123");
        assertThat(verification.getPurpose()).isEqualTo(VerificationPurpose.SIGN_UP);
        assertThat(verification.getIdempotencyKey()).isEqualTo("idem-123");
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(verification.getRequestedAt()).isEqualTo(requestedAt);
        assertThat(verification.getProvider()).isNull();
        assertThat(verification.getCompletedAt()).isNull();

        assertThat(Arrays.stream(Verification.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("name", "phoneNumber", "birthDate");
    }

    @Test
    void routesRequestedVerification() {
        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 25, 12, 0)
        );

        LocalDateTime routedAt = LocalDateTime.of(2026, 4, 25, 12, 1);

        verification.routeTo(ProviderType.NICE, 3L, routedAt);

        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.ROUTED);
        assertThat(verification.getProvider()).isEqualTo(ProviderType.NICE);
        assertThat(verification.getRoutingPolicyVersion()).isEqualTo(3L);
        assertThat(verification.getRoutedAt()).isEqualTo(routedAt);
    }

    @Test
    void progressesRoutedVerificationToInProgress() {
        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 25, 12, 0)
        );
        verification.routeTo(ProviderType.NICE, 3L, LocalDateTime.of(2026, 4, 25, 12, 1));

        LocalDateTime providerCalledAt = LocalDateTime.of(2026, 4, 25, 12, 2);

        verification.startProviderCall("nice-tx-123", "nice-req-123", providerCalledAt);

        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.IN_PROGRESS);
        assertThat(verification.getProviderTransactionId()).isEqualTo("nice-tx-123");
        assertThat(verification.getProviderRequestNo()).isEqualTo("nice-req-123");
        assertThat(verification.getProviderCalledAt()).isEqualTo(providerCalledAt);
    }

    @Test
    void completesInProgressVerificationWithSuccess() {
        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 25, 12, 0)
        );
        verification.routeTo(ProviderType.NICE, 3L, LocalDateTime.of(2026, 4, 25, 12, 1));
        verification.startProviderCall("nice-tx-123", "nice-req-123", LocalDateTime.of(2026, 4, 25, 12, 2));

        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 25, 12, 5);

        verification.succeed(completedAt);

        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.SUCCESS);
        assertThat(verification.getCompletedAt()).isEqualTo(completedAt);
        assertThat(verification.isTerminal()).isTrue();
    }

    @Test
    void rehydratesExistingInProgressVerification() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 4, 25, 12, 0);
        LocalDateTime routedAt = LocalDateTime.of(2026, 4, 25, 12, 1);
        LocalDateTime providerCalledAt = LocalDateTime.of(2026, 4, 25, 12, 2);

        Verification verification = Verification.rehydrate(
                10L,
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                ProviderType.NICE,
                VerificationStatus.IN_PROGRESS,
                "nice-tx-123",
                "nice-req-123",
                null,
                3L,
                requestedAt,
                routedAt,
                providerCalledAt,
                null,
                7L
        );

        assertThat(verification.getId()).isEqualTo(10L);
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.IN_PROGRESS);
        assertThat(verification.getProvider()).isEqualTo(ProviderType.NICE);
        assertThat(verification.getProviderTransactionId()).isEqualTo("nice-tx-123");
        assertThat(verification.getProviderRequestNo()).isEqualTo("nice-req-123");
        assertThat(verification.getWebTransactionId()).isNull();
        assertThat(verification.getVersion()).isEqualTo(7L);
    }

    @Test
    void recordsProviderReturnWebTransactionId() {
        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 25, 12, 0)
        );
        verification.routeTo(ProviderType.NICE, 3L, LocalDateTime.of(2026, 4, 25, 12, 1));
        verification.startProviderCall("nice-tx-123", "nice-req-123", LocalDateTime.of(2026, 4, 25, 12, 2));

        verification.recordProviderReturn("nice-web-tx-123");

        assertThat(verification.getWebTransactionId()).isEqualTo("nice-web-tx-123");
    }

    @Test
    void cancelsRequestedVerification() {
        Verification verification = Verification.requested(
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                LocalDateTime.of(2026, 4, 25, 12, 0)
        );

        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 25, 12, 5);

        verification.cancel(completedAt);

        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.CANCELED);
        assertThat(verification.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void rejectsInvalidRehydratedInProgressVerificationWithoutProviderTransactionId() {
        assertThatThrownBy(() -> Verification.rehydrate(
                10L,
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                ProviderType.NICE,
                VerificationStatus.IN_PROGRESS,
                null,
                "nice-req-123",
                null,
                3L,
                LocalDateTime.of(2026, 4, 25, 12, 0),
                LocalDateTime.of(2026, 4, 25, 12, 1),
                LocalDateTime.of(2026, 4, 25, 12, 2),
                null,
                7L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerTransactionId");
    }

    @Test
    void rejectsInvalidRehydratedInProgressVerificationWithoutProviderRequestNo() {
        assertThatThrownBy(() -> Verification.rehydrate(
                10L,
                "verif_123",
                "user-123",
                VerificationPurpose.SIGN_UP,
                "idem-123",
                ProviderType.NICE,
                VerificationStatus.IN_PROGRESS,
                "nice-tx-123",
                null,
                null,
                3L,
                LocalDateTime.of(2026, 4, 25, 12, 0),
                LocalDateTime.of(2026, 4, 25, 12, 1),
                LocalDateTime.of(2026, 4, 25, 12, 2),
                null,
                7L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerRequestNo");
    }
}
