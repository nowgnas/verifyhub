package com.verifyhub.routing.domain;

import com.verifyhub.verification.domain.ProviderType;

public record ProviderRoutingPolicy(
        ProviderType provider,
        int weight,
        boolean enabled,
        long version
) {
}
