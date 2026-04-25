package com.verifyhub.verification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record LateCallbackHistory(
        Long id,
        String verificationId,
        ProviderType provider,
        VerificationStatus currentStatus,
        String callbackResult,
        boolean duplicate,
        String rawPayload,
        String reason,
        LocalDateTime createdAt
) {

    public LateCallbackHistory {
        requireText(verificationId, "verificationId");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        requireText(callbackResult, "callbackResult");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
