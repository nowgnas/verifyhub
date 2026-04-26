package com.verifyhub.verification.adapter.out.persistence;

import com.verifyhub.verification.adapter.out.persistence.mapper.ProviderCallHistoryPersistenceMapper;
import com.verifyhub.verification.adapter.out.persistence.repository.ProviderCallHistoryJpaRepository;
import com.verifyhub.verification.domain.ProviderCallHistory;
import com.verifyhub.verification.port.out.ProviderCallHistoryRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class ProviderCallHistoryPersistenceAdapter implements ProviderCallHistoryRepositoryPort {

    private final ProviderCallHistoryJpaRepository providerCallHistoryJpaRepository;
    private final ProviderCallHistoryPersistenceMapper providerCallHistoryPersistenceMapper;

    public ProviderCallHistoryPersistenceAdapter(
            ProviderCallHistoryJpaRepository providerCallHistoryJpaRepository,
            ProviderCallHistoryPersistenceMapper providerCallHistoryPersistenceMapper
    ) {
        this.providerCallHistoryJpaRepository = providerCallHistoryJpaRepository;
        this.providerCallHistoryPersistenceMapper = providerCallHistoryPersistenceMapper;
    }

    @Override
    public ProviderCallHistory save(ProviderCallHistory providerCallHistory) {
        return providerCallHistoryPersistenceMapper.toDomain(
                providerCallHistoryJpaRepository.save(providerCallHistoryPersistenceMapper.toEntity(providerCallHistory))
        );
    }
}
