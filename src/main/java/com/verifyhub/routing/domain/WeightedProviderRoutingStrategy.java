package com.verifyhub.routing.domain;

import com.verifyhub.common.exception.ProviderUnavailableException;
import com.verifyhub.verification.domain.ProviderType;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public class WeightedProviderRoutingStrategy {

    private final RandomBoundedNumberGenerator randomBoundedNumberGenerator;

    public WeightedProviderRoutingStrategy() {
        this(new SecureRandom()::nextInt);
    }

    public WeightedProviderRoutingStrategy(RandomBoundedNumberGenerator randomBoundedNumberGenerator) {
        this.randomBoundedNumberGenerator = randomBoundedNumberGenerator;
    }

    public RoutingDecision select(List<ProviderRoutingPolicy> candidates) {
        if (candidates.isEmpty()) {
            throw new ProviderUnavailableException();
        }

        int totalWeight = candidates.stream()
                .mapToInt(ProviderRoutingPolicy::weight)
                .sum();
        if (totalWeight <= 0) {
            throw new ProviderUnavailableException();
        }

        int ticket = randomBoundedNumberGenerator.nextInt(totalWeight);
        int accumulated = 0;
        for (ProviderRoutingPolicy candidate : candidates) {
            accumulated += candidate.weight();
            if (ticket < accumulated) {
                return selected(candidate.provider(), candidates);
            }
        }

        throw new ProviderUnavailableException();
    }

    private RoutingDecision selected(ProviderType provider, List<ProviderRoutingPolicy> candidates) {
        return new RoutingDecision(
                Optional.of(provider),
                RoutingReason.WEIGHTED_SELECTED,
                candidates.get(0).version(),
                candidates.stream().map(ProviderRoutingPolicy::provider).toList()
        );
    }
}
