package com.verifyhub.verification.application;

import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.verification.domain.Verification;
import java.util.List;
import java.util.Objects;

public record ProviderVerificationCommand(
        Verification verification,
        String returnUrl,
        String closeUrl,
        List<String> svcTypes,
        List<ProviderHealthSnapshot> providerHealthSnapshots
) {

    public ProviderVerificationCommand {
        Objects.requireNonNull(verification, "verification must not be null");
        requireText(returnUrl, "returnUrl");
        svcTypes = List.copyOf(Objects.requireNonNull(svcTypes, "svcTypes must not be null"));
        providerHealthSnapshots = List.copyOf(Objects.requireNonNull(providerHealthSnapshots, "providerHealthSnapshots must not be null"));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
