package com.verifyhub.routing.domain;

import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RoutingDecision(
        Optional<ProviderType> selectedProvider,
        RoutingReason reason,
        Long policyVersion,
        List<ProviderType> candidateProviders
) {

    public RoutingDecision {
        selectedProvider = Objects.requireNonNull(selectedProvider, "selectedProvider must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        candidateProviders = List.copyOf(Objects.requireNonNull(candidateProviders, "candidateProviders must not be null"));
    }
}
