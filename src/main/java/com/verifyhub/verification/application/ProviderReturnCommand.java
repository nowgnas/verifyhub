package com.verifyhub.verification.application;

import com.verifyhub.verification.domain.ProviderType;
import java.util.Objects;

public record ProviderReturnCommand(
        ProviderType provider,
        String verificationId,
        String webTransactionId
) {

    public ProviderReturnCommand {
        Objects.requireNonNull(provider, "provider must not be null");
        requireText(verificationId, "verificationId");
        requireText(webTransactionId, "webTransactionId");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
