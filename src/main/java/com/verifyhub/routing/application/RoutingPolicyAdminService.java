package com.verifyhub.routing.application;

import com.verifyhub.common.exception.InvalidRequestException;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.routing.port.out.ProviderRoutingPolicyRepositoryPort;
import com.verifyhub.verification.domain.ProviderType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingPolicyAdminService {

    private final ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort;

    public RoutingPolicyAdminService(ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort) {
        this.providerRoutingPolicyRepositoryPort = providerRoutingPolicyRepositoryPort;
    }

    @Transactional(readOnly = true)
    public List<ProviderRoutingPolicy> getLatestPolicies() {
        return providerRoutingPolicyRepositoryPort.findLatestPolicies();
    }

    @Transactional
    public List<ProviderRoutingPolicy> updatePolicies(RoutingPolicyUpdateCommand command) {
        validateCompleteProviderSet(command.policies());
        long nextVersion = providerRoutingPolicyRepositoryPort.findLatestVersion() + 1;
        List<ProviderRoutingPolicy> policies = command.policies().stream()
                .map(policy -> new ProviderRoutingPolicy(
                        policy.provider(),
                        policy.weight(),
                        policy.enabled(),
                        nextVersion
                ))
                .toList();
        return providerRoutingPolicyRepositoryPort.saveAll(policies);
    }

    private void validateCompleteProviderSet(List<RoutingPolicyUpdateCommand.RoutingPolicyItem> policies) {
        if (policies == null || policies.size() != ProviderType.values().length) {
            throw new InvalidRequestException("routing policy must include every provider exactly once");
        }
        Set<ProviderType> providers = EnumSet.noneOf(ProviderType.class);
        for (RoutingPolicyUpdateCommand.RoutingPolicyItem policy : policies) {
            if (policy.weight() < 0) {
                throw new InvalidRequestException("routing policy weight must be zero or greater");
            }
            if (policy.enabled() && policy.weight() == 0) {
                throw new InvalidRequestException("enabled routing policy weight must be greater than zero");
            }
            if (!providers.add(policy.provider())) {
                throw new InvalidRequestException("routing policy provider must not be duplicated");
            }
        }
        if (!providers.equals(EnumSet.copyOf(Arrays.asList(ProviderType.values())))) {
            throw new InvalidRequestException("routing policy must include every provider exactly once");
        }
    }
}
