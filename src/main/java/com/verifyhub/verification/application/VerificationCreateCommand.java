package com.verifyhub.verification.application;

import com.verifyhub.verification.domain.VerificationPurpose;
import java.util.List;
import java.util.Objects;

public record VerificationCreateCommand(
        String requestId,
        VerificationPurpose purpose,
        String idempotencyKey,
        String returnUrl,
        String closeUrl,
        List<String> svcTypes
) {

    public VerificationCreateCommand {
        requireText(requestId, "requestId");
        Objects.requireNonNull(purpose, "purpose must not be null");
        requireText(idempotencyKey, "idempotencyKey");
        requireText(returnUrl, "returnUrl");
        svcTypes = List.copyOf(Objects.requireNonNull(svcTypes, "svcTypes must not be null"));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
