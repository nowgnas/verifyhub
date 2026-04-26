package com.verifyhub.verification.domain;

import java.util.Objects;

public record ProviderResultRequest(
        ProviderType provider,
        String providerTransactionId,
        String providerRequestNo,
        String verificationId,
        String webTransactionId
) {

    public ProviderResultRequest {
        Objects.requireNonNull(provider, "provider must not be null");
        requireText(providerTransactionId, "providerTransactionId");
        requireText(providerRequestNo, "providerRequestNo");
        requireText(verificationId, "verificationId");
        requireText(webTransactionId, "webTransactionId");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
