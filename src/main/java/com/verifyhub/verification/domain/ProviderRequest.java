package com.verifyhub.verification.domain;

import java.util.Objects;

public record ProviderRequest(
        String verificationId,
        String requestId,
        String name,
        String phoneNumber,
        String birthDate,
        String purpose
) {

    public ProviderRequest {
        requireText(verificationId, "verificationId");
        requireText(requestId, "requestId");
        requireText(purpose, "purpose");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
