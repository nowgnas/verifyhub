package com.verifyhub.routing.adapter.out.persistence;

import com.verifyhub.routing.adapter.out.persistence.mapper.ProviderRoutingPolicyPersistenceMapper;
import com.verifyhub.routing.adapter.out.persistence.repository.ProviderRoutingPolicyJpaRepository;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.routing.port.out.ProviderRoutingPolicyRepositoryPort;
import com.verifyhub.verification.adapter.out.persistence.entity.ProviderRoutingPolicyEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProviderRoutingPolicyPersistenceAdapter implements ProviderRoutingPolicyRepositoryPort {

    private final ProviderRoutingPolicyJpaRepository providerRoutingPolicyJpaRepository;
    private final ProviderRoutingPolicyPersistenceMapper providerRoutingPolicyPersistenceMapper;

    public ProviderRoutingPolicyPersistenceAdapter(
            ProviderRoutingPolicyJpaRepository providerRoutingPolicyJpaRepository,
            ProviderRoutingPolicyPersistenceMapper providerRoutingPolicyPersistenceMapper
    ) {
        this.providerRoutingPolicyJpaRepository = providerRoutingPolicyJpaRepository;
        this.providerRoutingPolicyPersistenceMapper = providerRoutingPolicyPersistenceMapper;
    }

    @Override
    public List<ProviderRoutingPolicy> findLatestEnabledPolicies() {
        return providerRoutingPolicyJpaRepository.findLatestEnabledPolicies().stream()
                .map(providerRoutingPolicyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProviderRoutingPolicy> findLatestPolicies() {
        return providerRoutingPolicyJpaRepository.findLatestPolicies().stream()
                .map(providerRoutingPolicyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long findLatestVersion() {
        Long latestVersion = providerRoutingPolicyJpaRepository.findLatestVersion();
        return latestVersion == null ? 0L : latestVersion;
    }

    @Override
    public List<ProviderRoutingPolicy> saveAll(List<ProviderRoutingPolicy> policies) {
        LocalDateTime now = LocalDateTime.now();
        return providerRoutingPolicyJpaRepository.saveAll(policies.stream()
                        .map(policy -> new ProviderRoutingPolicyEntity(
                                null,
                                policy.provider(),
                                policy.weight(),
                                policy.enabled(),
                                policy.version(),
                                now,
                                now
                        ))
                        .toList()).stream()
                .map(providerRoutingPolicyPersistenceMapper::toDomain)
                .toList();
    }
}
