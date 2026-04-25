package com.verifyhub.verification.domain;

import static org.assertj.core.api.Assertions.assertThat;

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
}
