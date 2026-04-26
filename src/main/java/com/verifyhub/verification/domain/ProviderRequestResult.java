package com.verifyhub.verification.domain;

import java.util.Objects;

public record ProviderRequestResult(
        ProviderType provider,
        String providerTransactionId,
        String providerRequestNo,
        String authUrl,
        ProviderRequestResultType resultType,
        String rawResponse,
        Integer httpStatus,
        Long latencyMs,
        String errorMessage
) {

    public ProviderRequestResult {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
    }
}
