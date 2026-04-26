package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.OutboxEvent;
import com.verifyhub.verification.domain.OutboxEventStatus;
import com.verifyhub.verification.port.out.OutboxEventPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OutboxEventServiceTest {

    @Test
    void enqueuesPendingOutboxEvent() {
        OutboxEventPort outboxEventPort = mock(OutboxEventPort.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        OutboxEventService service = new OutboxEventService(outboxEventPort, timeProvider);
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 14, 2);
        when(timeProvider.now()).thenReturn(now);
        when(outboxEventPort.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OutboxEvent saved = service.enqueue("Verification", "verif-1", "VerificationRequested", "{\"a\":1}");

        assertThat(saved.aggregateType()).isEqualTo("Verification");
        assertThat(saved.aggregateId()).isEqualTo("verif-1");
        assertThat(saved.status()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(saved.retryCount()).isZero();
        assertThat(saved.createdAt()).isEqualTo(now);
        assertThat(saved.publishedAt()).isNull();
    }
}
