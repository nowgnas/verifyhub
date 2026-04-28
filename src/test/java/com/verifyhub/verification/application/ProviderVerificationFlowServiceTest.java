package com.verifyhub.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.routing.application.ProviderRoutingService;
import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.routing.domain.RoutingDecision;
import com.verifyhub.routing.domain.RoutingReason;
import com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecorator;
import com.verifyhub.verification.domain.ProviderCallResultType;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.ProviderClientPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProviderVerificationFlowServiceTest {

    private final ProviderRoutingService providerRoutingService = mock(ProviderRoutingService.class);
    private final VerificationStateService verificationStateService = mock(VerificationStateService.class);
    private final ProviderCallHistoryService providerCallHistoryService = mock(ProviderCallHistoryService.class);
    private final ProviderClientPort niceProviderClient = mock(ProviderClientPort.class);
    private final ProviderClientResilienceDecorator resilienceDecorator = mock(ProviderClientResilienceDecorator.class);
    private final TimeProvider timeProvider = mock(TimeProvider.class);

    @Test
    void routesVerificationAndStartsProviderCallWhenProviderAcceptsRequest() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 26, 19, 30);
        Verification requested = Verification.requested(
                "verif-1",
                "req-1",
                VerificationPurpose.LOGIN,
                "idem-1",
                LocalDateTime.of(2026, 4, 26, 19, 29)
        );
        ProviderRequestResult providerResult = new ProviderRequestResult(
                ProviderType.NICE,
                "nice-tx-1",
                "nice-req-1",
                "https://nice.example/auth",
                ProviderRequestResultType.ACCEPTED,
                "{\"resultType\":\"ACCEPTED\"}",
                200,
                100L,
                null
        );

        when(providerRoutingService.route(List.of(ProviderHealthSnapshot.available(ProviderType.NICE))))
                .thenReturn(new RoutingDecision(Optional.of(ProviderType.NICE), RoutingReason.WEIGHTED_SELECTED, 1L, List.of(ProviderType.NICE)));
        when(niceProviderClient.providerType()).thenReturn(ProviderType.NICE);
        ProviderVerificationFlowService service = new ProviderVerificationFlowService(
                providerRoutingService,
                verificationStateService,
                providerCallHistoryService,
                List.of(niceProviderClient),
                resilienceDecorator,
                timeProvider
        );
        when(timeProvider.now()).thenReturn(now);
        when(resilienceDecorator.requestVerification(any(), any())).thenReturn(providerResult);
        when(verificationStateService.routeTo("verif-1", ProviderType.NICE, 1L, now)).thenReturn(requested);
        when(verificationStateService.startProviderCall("verif-1", "nice-tx-1", "nice-req-1", now)).thenReturn(
                Verification.rehydrate(
                        1L,
                        "verif-1",
                        "req-1",
                        VerificationPurpose.LOGIN,
                        "idem-1",
                        ProviderType.NICE,
                        VerificationStatus.IN_PROGRESS,
                        "nice-tx-1",
                        "nice-req-1",
                        null,
                        1L,
                        LocalDateTime.of(2026, 4, 26, 19, 29),
                        now,
                        now,
                        null,
                        1L
                )
        );

        ProviderVerificationResult result = service.requestProviderVerification(
                new ProviderVerificationCommand(
                        requested,
                        "tester",
                        "01012345678",
                        "19900101",
                        List.of(ProviderHealthSnapshot.available(ProviderType.NICE))
                )
        );

        assertThat(result.status()).isEqualTo(VerificationStatus.IN_PROGRESS);
        assertThat(result.provider()).isEqualTo(ProviderType.NICE);
        assertThat(result.authUrl()).isEqualTo("https://nice.example/auth");
        verify(resilienceDecorator).requestVerification(niceProviderClient, new ProviderRequest(
                "verif-1",
                "req-1",
                "tester",
                "01012345678",
                "19900101",
                "LOGIN"
        ));
        verify(providerCallHistoryService).record(
                "verif-1",
                ProviderType.NICE,
                "{\"verificationId\":\"verif-1\",\"requestId\":\"req-1\",\"name\":\"tester\",\"phoneNumber\":\"01012345678\",\"birthDate\":\"19900101\",\"purpose\":\"LOGIN\"}",
                "{\"resultType\":\"ACCEPTED\"}",
                200,
                ProviderCallResultType.ACCEPTED,
                100L,
                null,
                0
        );
    }
}
