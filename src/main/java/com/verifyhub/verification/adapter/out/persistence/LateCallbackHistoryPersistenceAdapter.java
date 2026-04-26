package com.verifyhub.verification.adapter.out.persistence;

import com.verifyhub.verification.adapter.out.persistence.mapper.LateCallbackHistoryPersistenceMapper;
import com.verifyhub.verification.adapter.out.persistence.repository.LateCallbackHistoryJpaRepository;
import com.verifyhub.verification.domain.LateCallbackHistory;
import com.verifyhub.verification.port.out.LateCallbackHistoryRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class LateCallbackHistoryPersistenceAdapter implements LateCallbackHistoryRepositoryPort {

    private final LateCallbackHistoryJpaRepository lateCallbackHistoryJpaRepository;
    private final LateCallbackHistoryPersistenceMapper lateCallbackHistoryPersistenceMapper;

    public LateCallbackHistoryPersistenceAdapter(
            LateCallbackHistoryJpaRepository lateCallbackHistoryJpaRepository,
            LateCallbackHistoryPersistenceMapper lateCallbackHistoryPersistenceMapper
    ) {
        this.lateCallbackHistoryJpaRepository = lateCallbackHistoryJpaRepository;
        this.lateCallbackHistoryPersistenceMapper = lateCallbackHistoryPersistenceMapper;
    }

    @Override
    public LateCallbackHistory save(LateCallbackHistory lateCallbackHistory) {
        return lateCallbackHistoryPersistenceMapper.toDomain(
                lateCallbackHistoryJpaRepository.save(lateCallbackHistoryPersistenceMapper.toEntity(lateCallbackHistory))
        );
    }
}
