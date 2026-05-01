package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.verifyhub.common.id.VerificationIdGenerator;
import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.idempotency.application.IdempotencyService;
import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VerificationCreateServiceTest {

    private final VerificationIdGenerator verificationIdGenerator = mock(VerificationIdGenerator.class);
    private final TimeProvider timeProvider = mock(TimeProvider.class);
    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);
    private final ProviderVerificationFlowService providerVerificationFlowService = mock(ProviderVerificationFlowService.class);

    @Test
    void createsVerificationAndRequestsProviderVerification() {
        VerificationCreateService service = new VerificationCreateService(
                verificationIdGenerator,
                timeProvider,
                idempotencyService,
                providerVerificationFlowService
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 1, 14, 0);
        when(verificationIdGenerator.generate()).thenReturn("verif-1");
        when(timeProvider.now()).thenReturn(now);
        when(idempotencyService.getOrCreate(
                eq("req-1"),
                eq(VerificationPurpose.LOGIN),
                eq("idem-1"),
                any()
        )).thenAnswer(invocation -> {
            Supplier<Verification> creator = invocation.getArgument(3);
            return creator.get();
        });
        ProviderAuthEntry authEntry = new ProviderAuthEntry(
                ProviderType.NICE,
                AuthEntryType.REDIRECT_URL,
                "https://nice.example/auth",
                "GET",
                "UTF-8",
                Map.of()
        );
        when(providerVerificationFlowService.requestProviderVerification(any(ProviderVerificationCommand.class)))
                .thenReturn(new ProviderVerificationResult(
                        "verif-1",
                        ProviderType.NICE,
                        VerificationStatus.IN_PROGRESS,
                        authEntry
                ));

        ProviderVerificationResult result = service.create(new VerificationCreateCommand(
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                "https://client.example/return",
                "https://client.example/close",
                List.of("M")
        ));

        assertThat(result.verificationId()).isEqualTo("verif-1");
        assertThat(result.status()).isEqualTo(VerificationStatus.IN_PROGRESS);
        ArgumentCaptor<ProviderVerificationCommand> commandCaptor = ArgumentCaptor.forClass(ProviderVerificationCommand.class);
        verify(providerVerificationFlowService).requestProviderVerification(commandCaptor.capture());
        ProviderVerificationCommand providerCommand = commandCaptor.getValue();
        assertThat(providerCommand.verification().getVerificationId()).isEqualTo("verif-1");
        assertThat(providerCommand.verification().getRequestId()).isEqualTo("req-1");
        assertThat(providerCommand.verification().getPurpose()).isEqualTo(VerificationPurpose.LOGIN);
        assertThat(providerCommand.verification().getIdempotencyKey()).isEqualTo("idem-1");
        assertThat(providerCommand.verification().getRequestedAt()).isEqualTo(now);
        assertThat(providerCommand.returnUrl()).isEqualTo("https://client.example/return");
        assertThat(providerCommand.closeUrl()).isEqualTo("https://client.example/close");
        assertThat(providerCommand.svcTypes()).containsExactly("M");
        assertThat(providerCommand.providerHealthSnapshots()).isEmpty();
    }

    @Test
    void reusesExistingVerificationForSameIdempotencyKey() {
        VerificationCreateService service = new VerificationCreateService(
                verificationIdGenerator,
                timeProvider,
                idempotencyService,
                providerVerificationFlowService
        );
        Verification existing = Verification.requested(
                "verif-existing",
                "req-2",
                VerificationPurpose.SIGN_UP,
                "idem-2",
                LocalDateTime.of(2026, 5, 1, 14, 10)
        );
        when(idempotencyService.getOrCreate(
                eq("req-2"),
                eq(VerificationPurpose.SIGN_UP),
                eq("idem-2"),
                any()
        )).thenReturn(existing);
        ProviderAuthEntry authEntry = new ProviderAuthEntry(
                ProviderType.KG,
                AuthEntryType.FORM_POST,
                "https://kg.example/auth",
                "POST",
                "EUC-KR",
                Map.of("Tradeid", "kg-req-2")
        );
        when(providerVerificationFlowService.requestProviderVerification(any(ProviderVerificationCommand.class)))
                .thenReturn(new ProviderVerificationResult(
                        "verif-existing",
                        ProviderType.KG,
                        VerificationStatus.IN_PROGRESS,
                        authEntry
                ));

        ProviderVerificationResult result = service.create(new VerificationCreateCommand(
                "req-2",
                VerificationPurpose.SIGN_UP,
                "idem-2",
                "https://client.example/return",
                null,
                List.of("M")
        ));

        assertThat(result.verificationId()).isEqualTo("verif-existing");
        ArgumentCaptor<ProviderVerificationCommand> commandCaptor = ArgumentCaptor.forClass(ProviderVerificationCommand.class);
        verify(providerVerificationFlowService).requestProviderVerification(commandCaptor.capture());
        ProviderVerificationCommand providerCommand = commandCaptor.getValue();
        assertThat(providerCommand.verification()).isSameAs(existing);
        assertThat(providerCommand.returnUrl()).isEqualTo("https://client.example/return");
        assertThat(providerCommand.closeUrl()).isNull();
        assertThat(providerCommand.svcTypes()).containsExactly("M");
        assertThat(providerCommand.providerHealthSnapshots()).isEmpty();
    }

    @Test
    void returnsExistingInProgressVerificationWithoutRepeatingProviderVerification() {
        VerificationCreateService service = new VerificationCreateService(
                verificationIdGenerator,
                timeProvider,
                idempotencyService,
                providerVerificationFlowService
        );
        Verification existing = Verification.rehydrate(
                1L,
                "verif-in-progress",
                "req-3",
                VerificationPurpose.LOGIN,
                "idem-3",
                ProviderType.NICE,
                VerificationStatus.IN_PROGRESS,
                "nice-tx-3",
                "nice-req-3",
                null,
                1L,
                LocalDateTime.of(2026, 5, 1, 14, 20),
                LocalDateTime.of(2026, 5, 1, 14, 21),
                LocalDateTime.of(2026, 5, 1, 14, 22),
                null,
                1L
        );
        when(idempotencyService.getOrCreate(
                eq("req-3"),
                eq(VerificationPurpose.LOGIN),
                eq("idem-3"),
                any()
        )).thenReturn(existing);

        ProviderVerificationResult result = service.create(new VerificationCreateCommand(
                "req-3",
                VerificationPurpose.LOGIN,
                "idem-3",
                "https://client.example/return",
                "https://client.example/close",
                List.of("M")
        ));

        assertThat(result.verificationId()).isEqualTo("verif-in-progress");
        assertThat(result.provider()).isEqualTo(ProviderType.NICE);
        assertThat(result.status()).isEqualTo(VerificationStatus.IN_PROGRESS);
        assertThat(result.authEntry()).isNull();
        verify(providerVerificationFlowService, never()).requestProviderVerification(any());
    }
}
