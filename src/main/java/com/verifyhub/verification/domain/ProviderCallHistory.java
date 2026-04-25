package com.verifyhub.verification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProviderCallHistory(
        Long id,
        String verificationId,
        ProviderType provider,
        String requestPayload,
        String responsePayload,
        Integer httpStatus,
        ProviderCallResultType resultType,
        Long latencyMs,
        String errorMessage,
        int retryCount,
        LocalDateTime createdAt
) {

    public ProviderCallHistory {
        requireText(verificationId, "verificationId");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
