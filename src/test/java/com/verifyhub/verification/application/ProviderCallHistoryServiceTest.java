package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.ProviderCallHistory;
import com.verifyhub.verification.domain.ProviderCallResultType;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.port.out.ProviderCallHistoryRepositoryPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ProviderCallHistoryServiceTest {

    @Test
    void recordsProviderCallHistory() {
        ProviderCallHistoryRepositoryPort repositoryPort = mock(ProviderCallHistoryRepositoryPort.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        ProviderCallHistoryService service = new ProviderCallHistoryService(repositoryPort, timeProvider);
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 14, 0);
        when(timeProvider.now()).thenReturn(now);
        when(repositoryPort.save(any(ProviderCallHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderCallHistory saved = service.record(
                "verif-1",
                ProviderType.NICE,
                "{\"req\":true}",
                "{\"res\":true}",
                200,
                ProviderCallResultType.SUCCESS,
                120L,
                null,
                0
        );

        assertThat(saved.verificationId()).isEqualTo("verif-1");
        assertThat(saved.provider()).isEqualTo(ProviderType.NICE);
        assertThat(saved.resultType()).isEqualTo(ProviderCallResultType.SUCCESS);
        assertThat(saved.createdAt()).isEqualTo(now);
    }
}
