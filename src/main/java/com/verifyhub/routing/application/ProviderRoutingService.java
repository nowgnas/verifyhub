package com.verifyhub.routing.application;

import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.routing.domain.RoutingDecision;
import com.verifyhub.routing.domain.RoutingReason;
import com.verifyhub.routing.domain.WeightedProviderRoutingStrategy;
import com.verifyhub.routing.port.out.ProviderRoutingPolicyRepositoryPort;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderRoutingService {

    private final ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort;
    private final WeightedProviderRoutingStrategy weightedProviderRoutingStrategy;

    @Autowired
    public ProviderRoutingService(ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort) {
        this(providerRoutingPolicyRepositoryPort, new WeightedProviderRoutingStrategy());
    }

    ProviderRoutingService(
            ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort,
            WeightedProviderRoutingStrategy weightedProviderRoutingStrategy
    ) {
        this.providerRoutingPolicyRepositoryPort = providerRoutingPolicyRepositoryPort;
        this.weightedProviderRoutingStrategy = weightedProviderRoutingStrategy;
    }

    public RoutingDecision route(List<ProviderHealthSnapshot> healthSnapshots) {
        List<ProviderRoutingPolicy> policies = providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies();
        if (policies.isEmpty()) {
            return new RoutingDecision(Optional.empty(), RoutingReason.NO_ENABLED_POLICY, null, List.of());
        }

        Map<ProviderType, ProviderHealthSnapshot> healthByProvider = healthSnapshots.stream()
                .collect(Collectors.toMap(ProviderHealthSnapshot::provider, Function.identity()));

        List<ProviderType> candidates = policies.stream()
                .filter(policy -> isAvailable(policy.provider(), healthByProvider))
                .map(ProviderRoutingPolicy::provider)
                .toList();

        RoutingReason reason = candidates.isEmpty()
                ? RoutingReason.NO_AVAILABLE_PROVIDER
                : RoutingReason.POLICY_LOADED;
        if (candidates.isEmpty()) {
            return new RoutingDecision(Optional.empty(), reason, policies.get(0).version(), List.of());
        }

        List<ProviderRoutingPolicy> candidatePolicies = policies.stream()
                .filter(policy -> candidates.contains(policy.provider()))
                .toList();
        return weightedProviderRoutingStrategy.select(candidatePolicies);
    }

    private boolean isAvailable(
            ProviderType provider,
            Map<ProviderType, ProviderHealthSnapshot> healthByProvider
    ) {
        return healthByProvider.getOrDefault(provider, ProviderHealthSnapshot.available(provider)).available();
    }
}
