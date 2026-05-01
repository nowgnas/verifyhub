package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VerificationQueryServiceTest {

    @Test
    void findsVerificationByVerificationId() {
        VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
        VerificationHistoryRepositoryPort historyRepositoryPort = mock(VerificationHistoryRepositoryPort.class);
        VerificationQueryService service = new VerificationQueryService(verificationRepositoryPort, historyRepositoryPort);
        Verification verification = Verification.requested(
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                LocalDateTime.of(2026, 5, 1, 10, 0)
        );
        when(verificationRepositoryPort.findByVerificationId("verif-1")).thenReturn(Optional.of(verification));

        Verification found = service.getVerification("verif-1");

        assertThat(found).isSameAs(verification);
    }

    @Test
    void throwsWhenVerificationDoesNotExist() {
        VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
        VerificationHistoryRepositoryPort historyRepositoryPort = mock(VerificationHistoryRepositoryPort.class);
        VerificationQueryService service = new VerificationQueryService(verificationRepositoryPort, historyRepositoryPort);
        when(verificationRepositoryPort.findByVerificationId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVerification("missing"))
                .isInstanceOf(VerificationNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void findsHistoriesInRepositoryOrder() {
        VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
        VerificationHistoryRepositoryPort historyRepositoryPort = mock(VerificationHistoryRepositoryPort.class);
        VerificationQueryService service = new VerificationQueryService(verificationRepositoryPort, historyRepositoryPort);
        Verification verification = Verification.requested(
                "verif-2",
                "req-2",
                VerificationPurpose.SIGN_UP,
                "idem-2",
                LocalDateTime.of(2026, 5, 1, 11, 0)
        );
        VerificationHistory first = VerificationHistory.of(
                "verif-2",
                null,
                VerificationStatus.REQUESTED,
                VerificationEvent.VERIFICATION_REQUESTED,
                "requested",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 11, 0)
        );
        VerificationHistory second = VerificationHistory.of(
                "verif-2",
                VerificationStatus.REQUESTED,
                VerificationStatus.ROUTED,
                VerificationEvent.ROUTE_SELECTED,
                "routed",
                null,
                null,
                LocalDateTime.of(2026, 5, 1, 11, 1)
        );
        when(verificationRepositoryPort.findByVerificationId("verif-2")).thenReturn(Optional.of(verification));
        when(historyRepositoryPort.findByVerificationIdOrderByCreatedAtAsc("verif-2")).thenReturn(List.of(first, second));

        List<VerificationHistory> histories = service.getHistories("verif-2");

        assertThat(histories).containsExactly(first, second);
    }
}
