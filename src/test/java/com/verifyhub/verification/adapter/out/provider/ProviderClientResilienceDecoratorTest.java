package com.verifyhub.verification.adapter.out.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.verification.domain.AuthEntryType;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.port.out.ProviderClientPort;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ProviderClientResilienceDecoratorTest {

    @Test
    void retriesProviderVerificationCall() {
        ProviderClientPort delegate = mock(ProviderClientPort.class);
        ProviderRequest request = new ProviderRequest(
                "verif-1",
                "req-1",
                "kg-req-1",
                "https://verifyhub.example/api/v1/providers/KG/noti",
                "https://verifyhub.example/api/v1/providers/KG/close",
                "LOGIN",
                List.of("M")
        );
        ProviderRequestResult expected = new ProviderRequestResult(
                ProviderType.KG,
                "kg-tx-1",
                "kg-req-1",
                new ProviderAuthEntry(
                        ProviderType.KG,
                        AuthEntryType.FORM_POST,
                        "https://auth.mobilians.co.kr/goCashMain.mcash",
                        "POST",
                        "EUC-KR",
                        Map.of("Tradeid", "kg-req-1")
                ),
                ProviderRequestResultType.ACCEPTED,
                "{}",
                200,
                10L,
                null
        );
        AtomicInteger attempts = new AtomicInteger();

        when(delegate.providerType()).thenReturn(ProviderType.KG);
        when(delegate.requestVerification(request)).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("temporary");
            }
            return expected;
        });

        ProviderClientResilienceDecorator decorator = new ProviderClientResilienceDecorator(
                CircuitBreakerRegistry.ofDefaults(),
                RetryRegistry.of(RetryConfig.custom()
                        .maxAttempts(2)
                        .waitDuration(Duration.ZERO)
                        .build()),
                TimeLimiterRegistry.ofDefaults(),
                Executors.newSingleThreadExecutor()
        );

        ProviderRequestResult result = decorator.requestVerification(delegate, request);

        assertThat(result).isEqualTo(expected);
        assertThat(attempts).hasValue(2);
        decorator.close();
    }
}
