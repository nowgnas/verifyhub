package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class VerificationHistoryServiceTest {

    @Test
    void recordsVerificationHistory() {
        VerificationHistoryRepositoryPort repositoryPort = mock(VerificationHistoryRepositoryPort.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        VerificationHistoryService service = new VerificationHistoryService(repositoryPort, timeProvider);
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 14, 3);
        when(timeProvider.now()).thenReturn(now);
        when(repositoryPort.save(any(VerificationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationHistory saved = service.record(
                "verif-3",
                VerificationStatus.REQUESTED,
                VerificationStatus.ROUTED,
                VerificationEvent.ROUTE_SELECTED,
                null,
                ProviderType.NICE,
                "{\"event\":\"route\"}"
        );

        assertThat(saved.verificationId()).isEqualTo("verif-3");
        assertThat(saved.fromStatus()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(saved.toStatus()).isEqualTo(VerificationStatus.ROUTED);
        assertThat(saved.createdAt()).isEqualTo(now);
    }
}
