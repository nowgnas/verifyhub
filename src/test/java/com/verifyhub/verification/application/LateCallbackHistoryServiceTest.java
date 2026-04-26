package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.LateCallbackHistory;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.LateCallbackHistoryRepositoryPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LateCallbackHistoryServiceTest {

    @Test
    void recordsLateCallbackHistory() {
        LateCallbackHistoryRepositoryPort repositoryPort = mock(LateCallbackHistoryRepositoryPort.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        LateCallbackHistoryService service = new LateCallbackHistoryService(repositoryPort, timeProvider);
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 14, 1);
        when(timeProvider.now()).thenReturn(now);
        when(repositoryPort.save(any(LateCallbackHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LateCallbackHistory saved = service.record(
                "verif-2",
                ProviderType.KG,
                VerificationStatus.TIMEOUT,
                "SUCCESS",
                true,
                "{\"raw\":true}",
                "duplicate callback"
        );

        assertThat(saved.verificationId()).isEqualTo("verif-2");
        assertThat(saved.provider()).isEqualTo(ProviderType.KG);
        assertThat(saved.currentStatus()).isEqualTo(VerificationStatus.TIMEOUT);
        assertThat(saved.createdAt()).isEqualTo(now);
    }
}
