package com.verifyhub.idempotency.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class IdempotencyServiceTest {

    private final VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
    private final IdempotencyService idempotencyService = new IdempotencyService(verificationRepositoryPort);

    @Test
    void returnsExistingVerificationWithoutCallingCreator() {
        Verification existing = verification("verif-existing");
        AtomicBoolean creatorCalled = new AtomicBoolean(false);
        when(verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1"
        )).thenReturn(Optional.of(existing));

        Verification result = idempotencyService.getOrCreate(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                () -> {
                    creatorCalled.set(true);
                    return verification("verif-created");
                }
        );

        assertThat(result).isSameAs(existing);
        assertThat(creatorCalled).isFalse();
        verify(verificationRepositoryPort, never()).save(any(Verification.class));
    }

    @Test
    void createsAndSavesVerificationWhenExistingVerificationDoesNotExist() {
        Verification created = verification("verif-created");
        when(verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1"
        )).thenReturn(Optional.<Verification>empty());
        when(verificationRepositoryPort.save(created)).thenReturn(created);

        Verification result = idempotencyService.getOrCreate(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                () -> created
        );

        assertThat(result).isSameAs(created);
        verify(verificationRepositoryPort).save(created);
    }

    @Test
    void returnsExistingVerificationAfterUniqueConstraintConflict() {
        Verification created = verification("verif-created");
        Verification concurrentlyCreated = verification("verif-concurrent");
        when(verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1"
        )).thenReturn(Optional.<Verification>empty())
                .thenReturn(Optional.of(concurrentlyCreated));
        when(verificationRepositoryPort.save(created)).thenThrow(new DataIntegrityViolationException("duplicate"));

        Verification result = idempotencyService.getOrCreate(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                () -> created
        );

        assertThat(result).isSameAs(concurrentlyCreated);
    }

    @Test
    void rethrowsUniqueConstraintConflictWhenVerificationCannotBeFoundAfterConflict() {
        Verification created = verification("verif-created");
        DataIntegrityViolationException duplicate = new DataIntegrityViolationException("duplicate");
        when(verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1"
        )).thenReturn(Optional.<Verification>empty())
                .thenReturn(Optional.empty());
        when(verificationRepositoryPort.save(created)).thenThrow(duplicate);

        assertThatThrownBy(() -> idempotencyService.getOrCreate(
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                () -> created
        )).isSameAs(duplicate);
    }

    private static Verification verification(String verificationId) {
        return Verification.requested(
                verificationId,
                "req_1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                LocalDateTime.of(2026, 4, 26, 15, 0)
        );
    }
}
