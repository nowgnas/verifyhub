package com.verifyhub.verification.adapter.out.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.port.out.ProviderClientPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public abstract class MockProviderHttpClient implements ProviderClientPort {

    private final ProviderType providerType;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    protected MockProviderHttpClient(
            ProviderType providerType,
            RestTemplateBuilder restTemplateBuilder,
            String baseUrl
    ) {
        this(providerType, restTemplateBuilder.build(), baseUrl, new ObjectMapper(), Clock.systemUTC());
    }

    MockProviderHttpClient(
            ProviderType providerType,
            RestTemplate restTemplate,
            String baseUrl,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.providerType = providerType;
        this.restTemplate = restTemplate;
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public ProviderType providerType() {
        return providerType;
    }

    @Override
    public ProviderRequestResult requestVerification(ProviderRequest request) {
        Instant startedAt = clock.instant();
        try {
            ResponseEntity<MockProviderVerificationResponse> response = restTemplate.postForEntity(
                    baseUrl + "/verifications",
                    request,
                    MockProviderVerificationResponse.class
            );
            MockProviderVerificationResponse body = response.getBody();
            return new ProviderRequestResult(
                    providerType,
                    body == null ? null : body.providerTransactionId(),
                    body == null ? null : body.providerRequestNo(),
                    body == null ? null : body.authUrl(),
                    body == null ? ProviderRequestResultType.ERROR : body.resultType(),
                    toRawResponse(body),
                    response.getStatusCodeValue(),
                    elapsedMillis(startedAt),
                    null
            );
        } catch (HttpStatusCodeException exception) {
            return failedRequestResult(exception, startedAt);
        }
    }

    @Override
    public ProviderResult requestResult(ProviderResultRequest request) {
        ResponseEntity<MockProviderResultResponse> response = restTemplate.postForEntity(
                baseUrl + "/results",
                request,
                MockProviderResultResponse.class
        );
        MockProviderResultResponse body = response.getBody();
        return new ProviderResult(
                providerType,
                body == null ? null : body.providerTransactionId(),
                body == null ? null : body.verificationId(),
                body == null ? ProviderResultStatus.FAIL : body.result(),
                body != null && body.integrityVerified(),
                toRawResponse(body)
        );
    }

    private ProviderRequestResult failedRequestResult(HttpStatusCodeException exception, Instant startedAt) {
        return new ProviderRequestResult(
                providerType,
                null,
                null,
                null,
                ProviderRequestResultType.ERROR,
                exception.getResponseBodyAsString(),
                exception.getRawStatusCode(),
                elapsedMillis(startedAt),
                exception.getMessage()
        );
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, clock.instant()).toMillis();
    }

    private String toRawResponse(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return value.toString();
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private record MockProviderVerificationResponse(
            String providerTransactionId,
            String providerRequestNo,
            String authUrl,
            ProviderRequestResultType resultType
    ) {
    }

    private record MockProviderResultResponse(
            String providerTransactionId,
            String verificationId,
            ProviderResultStatus result,
            boolean integrityVerified
    ) {
    }
}
