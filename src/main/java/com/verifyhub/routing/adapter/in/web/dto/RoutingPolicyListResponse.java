package com.verifyhub.routing.adapter.in.web.dto;

import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.verification.domain.ProviderType;
import java.util.Comparator;
import java.util.List;

public record RoutingPolicyListResponse(
        long version,
        List<RoutingPolicyItemResponse> policies
) {

    public static RoutingPolicyListResponse from(List<ProviderRoutingPolicy> policies) {
        long version = policies.stream()
                .mapToLong(ProviderRoutingPolicy::version)
                .max()
                .orElse(0L);
        return new RoutingPolicyListResponse(
                version,
                policies.stream()
                        .sorted(Comparator.comparing(ProviderRoutingPolicy::provider))
                        .map(RoutingPolicyItemResponse::from)
                        .toList()
        );
    }

    public record RoutingPolicyItemResponse(
            ProviderType provider,
            int weight,
            boolean enabled
    ) {

        private static RoutingPolicyItemResponse from(ProviderRoutingPolicy policy) {
            return new RoutingPolicyItemResponse(
                    policy.provider(),
                    policy.weight(),
                    policy.enabled()
            );
        }
    }
}
