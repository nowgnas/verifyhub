package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.verifyhub.common.exception.InvalidStateTransitionException;
import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VerificationStateServiceTest {

    private final VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
    private final VerificationHistoryRepositoryPort verificationHistoryRepositoryPort = mock(VerificationHistoryRepositoryPort.class);
    private final VerificationStateService verificationStateService = new VerificationStateService(
            verificationRepositoryPort,
            verificationHistoryRepositoryPort
    );

    @Test
    void routesRequestedVerificationAndStoresHistory() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 4, 26, 12, 0);
        LocalDateTime routedAt = LocalDateTime.of(2026, 4, 26, 12, 1);
        Verification requested = Verification.requested(
                "verif-1",
                "user-1",
                VerificationPurpose.SIGN_UP,
                "idem-1",
                requestedAt
        );

        when(verificationRepositoryPort.findByVerificationId("verif-1")).thenReturn(Optional.of(requested));
        when(verificationRepositoryPort.save(any(Verification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Verification routed = verificationStateService.routeTo("verif-1", ProviderType.NICE, 1L, routedAt);

        assertThat(routed.getStatus()).isEqualTo(VerificationStatus.ROUTED);
        assertThat(routed.getProvider()).isEqualTo(ProviderType.NICE);
        verify(verificationRepositoryPort).save(routed);
        verify(verificationHistoryRepositoryPort).save(any(VerificationHistory.class));
    }

    @Test
    void doesNotStoreHistoryWhenTransitionFails() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 4, 26, 12, 0);
        Verification requested = Verification.requested(
                "verif-2",
                "user-2",
                VerificationPurpose.SIGN_UP,
                "idem-2",
                requestedAt
        );

        when(verificationRepositoryPort.findByVerificationId("verif-2")).thenReturn(Optional.of(requested));

        assertThatThrownBy(() -> verificationStateService.markSuccess("verif-2", LocalDateTime.of(2026, 4, 26, 12, 5)))
                .isInstanceOf(InvalidStateTransitionException.class);
        verify(verificationRepositoryPort, never()).save(any(Verification.class));
        verify(verificationHistoryRepositoryPort, never()).save(any(VerificationHistory.class));
    }

    @Test
    void throwsWhenVerificationDoesNotExist() {
        when(verificationRepositoryPort.findByVerificationId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationStateService.routeTo(
                "missing",
                ProviderType.KG,
                1L,
                LocalDateTime.of(2026, 4, 26, 12, 1)
        )).isInstanceOf(VerificationNotFoundException.class);
    }
}
