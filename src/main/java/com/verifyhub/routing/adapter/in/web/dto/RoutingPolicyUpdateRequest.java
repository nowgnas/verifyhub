package com.verifyhub.routing.adapter.in.web.dto;

import com.verifyhub.routing.application.RoutingPolicyUpdateCommand;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record RoutingPolicyUpdateRequest(
        @NotEmpty List<@Valid RoutingPolicyItemRequest> policies
) {

    public RoutingPolicyUpdateCommand toCommand() {
        return new RoutingPolicyUpdateCommand(policies.stream()
                .map(policy -> new RoutingPolicyUpdateCommand.RoutingPolicyItem(
                        policy.provider(),
                        policy.weight(),
                        policy.enabled()
                ))
                .toList());
    }

    public record RoutingPolicyItemRequest(
            @NotNull ProviderType provider,
            @Min(0) int weight,
            @NotNull Boolean enabled
    ) {
    }
}
