package com.verifyhub.routing.adapter.out.persistence;

import com.verifyhub.routing.adapter.out.persistence.mapper.ProviderRoutingPolicyPersistenceMapper;
import com.verifyhub.routing.adapter.out.persistence.repository.ProviderRoutingPolicyJpaRepository;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.routing.port.out.ProviderRoutingPolicyRepositoryPort;
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
}
