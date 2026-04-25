package com.verifyhub.verification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record VerificationHistory(
        Long id,
        String verificationId,
        VerificationStatus fromStatus,
        VerificationStatus toStatus,
        VerificationEvent eventType,
        String reason,
        ProviderType provider,
        String rawPayload,
        LocalDateTime createdAt
) {

    public VerificationHistory {
        requireText(verificationId, "verificationId");
        Objects.requireNonNull(toStatus, "toStatus must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static VerificationHistory of(
            String verificationId,
            VerificationStatus fromStatus,
            VerificationStatus toStatus,
            VerificationEvent eventType,
            String reason,
            ProviderType provider,
            String rawPayload,
            LocalDateTime createdAt
    ) {
        return new VerificationHistory(null, verificationId, fromStatus, toStatus, eventType, reason, provider, rawPayload, createdAt);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
