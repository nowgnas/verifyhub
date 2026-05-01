package com.verifyhub.verification.domain;

import java.util.List;
import java.util.Objects;

public record ProviderRequest(
        String verificationId,
        String requestId,
        String providerRequestNo,
        String returnUrl,
        String closeUrl,
        String purpose,
        List<String> svcTypes
) {

    public ProviderRequest {
        requireText(verificationId, "verificationId");
        requireText(requestId, "requestId");
        requireText(providerRequestNo, "providerRequestNo");
        requireText(returnUrl, "returnUrl");
        requireText(purpose, "purpose");
        svcTypes = List.copyOf(Objects.requireNonNull(svcTypes, "svcTypes must not be null"));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
