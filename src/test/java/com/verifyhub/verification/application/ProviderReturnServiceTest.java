package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.common.exception.InvalidRequestException;
import com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecorator;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.ProviderClientPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProviderReturnServiceTest {

    private final VerificationRepositoryPort verificationRepositoryPort = mock(VerificationRepositoryPort.class);
    private final VerificationStateService verificationStateService = mock(VerificationStateService.class);
    private final ProviderClientPort niceProviderClient = mock(ProviderClientPort.class);
    private final ProviderClientResilienceDecorator resilienceDecorator = mock(ProviderClientResilienceDecorator.class);
    private final OutboxEventService outboxEventService = mock(OutboxEventService.class);
    private final TimeProvider timeProvider = mock(TimeProvider.class);

    @Test
    void retrievesProviderResultAndMarksVerificationSuccess() {
        when(niceProviderClient.providerType()).thenReturn(ProviderType.NICE);
        ProviderReturnService service = new ProviderReturnService(
                verificationRepositoryPort,
                verificationStateService,
                List.of(niceProviderClient),
                resilienceDecorator,
                outboxEventService,
                timeProvider
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 4, 10, 3);
        Verification inProgress = inProgressVerification(ProviderType.NICE);
        when(verificationRepositoryPort.findByVerificationId("verif-1")).thenReturn(Optional.of(inProgress));
        when(timeProvider.now()).thenReturn(now);
        when(resilienceDecorator.requestResult(niceProviderClient, new ProviderResultRequest(
                ProviderType.NICE,
                "nice-tx-1",
                "nice-req-1",
                "verif-1",
                "web-tx-1"
        ))).thenReturn(new ProviderResult(
                ProviderType.NICE,
                "nice-tx-1",
                "verif-1",
                ProviderResultStatus.SUCCESS,
                true,
                "{\"result\":\"SUCCESS\"}"
        ));
        when(verificationStateService.markSuccess(any(), any())).thenReturn(successVerification());

        ProviderReturnResult result = service.handleReturn(new ProviderReturnCommand(
                ProviderType.NICE,
                "verif-1",
                "web-tx-1"
        ));

        assertThat(result.verificationId()).isEqualTo("verif-1");
        assertThat(result.provider()).isEqualTo(ProviderType.NICE);
        assertThat(result.status()).isEqualTo(VerificationStatus.SUCCESS);
        assertThat(result.result()).isEqualTo(ProviderResultStatus.SUCCESS);
        assertThat(result.integrityVerified()).isTrue();
        verify(verificationStateService).recordProviderReturn(
                "verif-1",
                "web-tx-1",
                VerificationEvent.CALLBACK_SUCCESS,
                "{\"result\":\"SUCCESS\"}"
        );
        verify(verificationStateService).markSuccess("verif-1", now);
        verify(outboxEventService).enqueue(
                "Verification",
                "verif-1",
                "VERIFICATION_SUCCEEDED",
                "{\"provider\":\"NICE\",\"result\":\"SUCCESS\"}"
        );
    }

    @Test
    void rejectsProviderPathMismatch() {
        when(niceProviderClient.providerType()).thenReturn(ProviderType.NICE);
        ProviderReturnService service = new ProviderReturnService(
                verificationRepositoryPort,
                verificationStateService,
                List.of(niceProviderClient),
                resilienceDecorator,
                outboxEventService,
                timeProvider
        );
        when(verificationRepositoryPort.findByVerificationId("verif-1"))
                .thenReturn(Optional.of(inProgressVerification(ProviderType.NICE)));

        assertThatThrownBy(() -> service.handleReturn(new ProviderReturnCommand(
                ProviderType.KG,
                "verif-1",
                "web-tx-1"
        )))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("provider mismatch");
    }

    @Test
    void marksFailWhenIntegrityVerificationFails() {
        when(niceProviderClient.providerType()).thenReturn(ProviderType.NICE);
        ProviderReturnService service = new ProviderReturnService(
                verificationRepositoryPort,
                verificationStateService,
                List.of(niceProviderClient),
                resilienceDecorator,
                outboxEventService,
                timeProvider
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 4, 10, 3);
        Verification inProgress = inProgressVerification(ProviderType.NICE);
        when(verificationRepositoryPort.findByVerificationId("verif-1")).thenReturn(Optional.of(inProgress));
        when(timeProvider.now()).thenReturn(now);
        when(resilienceDecorator.requestResult(any(), any())).thenReturn(new ProviderResult(
                ProviderType.NICE,
                "nice-tx-1",
                "verif-1",
                ProviderResultStatus.SUCCESS,
                false,
                "{\"result\":\"SUCCESS\",\"integrityVerified\":false}"
        ));
        when(verificationStateService.markFail(any(), any(), any())).thenReturn(failVerification());

        ProviderReturnResult result = service.handleReturn(new ProviderReturnCommand(
                ProviderType.NICE,
                "verif-1",
                "web-tx-1"
        ));

        assertThat(result.status()).isEqualTo(VerificationStatus.FAIL);
        assertThat(result.result()).isEqualTo(ProviderResultStatus.SUCCESS);
        assertThat(result.integrityVerified()).isFalse();
        verify(verificationStateService).recordProviderReturn(
                "verif-1",
                "web-tx-1",
                VerificationEvent.CALLBACK_FAIL,
                "{\"result\":\"SUCCESS\",\"integrityVerified\":false}"
        );
        verify(verificationStateService).markFail(
                "verif-1",
                now,
                "provider result integrity verification failed"
        );
    }

    private Verification inProgressVerification(ProviderType provider) {
        return Verification.rehydrate(
                1L,
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                provider,
                VerificationStatus.IN_PROGRESS,
                provider.name().toLowerCase() + "-tx-1",
                provider.name().toLowerCase() + "-req-1",
                null,
                1L,
                LocalDateTime.of(2026, 5, 4, 10, 0),
                LocalDateTime.of(2026, 5, 4, 10, 1),
                LocalDateTime.of(2026, 5, 4, 10, 2),
                null,
                1L
        );
    }

    private Verification successVerification() {
        return Verification.rehydrate(
                1L,
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                ProviderType.NICE,
                VerificationStatus.SUCCESS,
                "nice-tx-1",
                "nice-req-1",
                "web-tx-1",
                1L,
                LocalDateTime.of(2026, 5, 4, 10, 0),
                LocalDateTime.of(2026, 5, 4, 10, 1),
                LocalDateTime.of(2026, 5, 4, 10, 2),
                LocalDateTime.of(2026, 5, 4, 10, 3),
                2L
        );
    }

    private Verification failVerification() {
        return Verification.rehydrate(
                1L,
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                ProviderType.NICE,
                VerificationStatus.FAIL,
                "nice-tx-1",
                "nice-req-1",
                "web-tx-1",
                1L,
                LocalDateTime.of(2026, 5, 4, 10, 0),
                LocalDateTime.of(2026, 5, 4, 10, 1),
                LocalDateTime.of(2026, 5, 4, 10, 2),
                LocalDateTime.of(2026, 5, 4, 10, 3),
                2L
        );
    }
}
