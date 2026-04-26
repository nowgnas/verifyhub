package com.verifyhub.verification.application;

import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.verification.domain.Verification;
import java.util.List;
import java.util.Objects;

public record ProviderVerificationCommand(
        Verification verification,
        String name,
        String phoneNumber,
        String birthDate,
        List<ProviderHealthSnapshot> providerHealthSnapshots
) {

    public ProviderVerificationCommand {
        Objects.requireNonNull(verification, "verification must not be null");
        providerHealthSnapshots = List.copyOf(Objects.requireNonNull(providerHealthSnapshots, "providerHealthSnapshots must not be null"));
    }
}
