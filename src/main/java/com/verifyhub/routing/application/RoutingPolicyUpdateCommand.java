package com.verifyhub.routing.application;

import com.verifyhub.verification.domain.ProviderType;
import java.util.List;

public record RoutingPolicyUpdateCommand(
        List<RoutingPolicyItem> policies
) {

    public record RoutingPolicyItem(
            ProviderType provider,
            int weight,
            boolean enabled
    ) {
    }
}
