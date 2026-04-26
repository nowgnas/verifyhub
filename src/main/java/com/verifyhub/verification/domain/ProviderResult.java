package com.verifyhub.verification.domain;

import java.util.Objects;

public record ProviderResult(
        ProviderType provider,
        String providerTransactionId,
        String verificationId,
        ProviderResultStatus result,
        boolean integrityVerified,
        String rawPayload
) {

    public ProviderResult {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(result, "result must not be null");
    }
}
