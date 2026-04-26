package com.verifyhub.verification.adapter.out.persistence;

import com.verifyhub.verification.adapter.out.persistence.mapper.VerificationHistoryPersistenceMapper;
import com.verifyhub.verification.adapter.out.persistence.repository.VerificationHistoryJpaRepository;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class VerificationHistoryPersistenceAdapter implements VerificationHistoryRepositoryPort {

    private final VerificationHistoryJpaRepository verificationHistoryJpaRepository;
    private final VerificationHistoryPersistenceMapper verificationHistoryPersistenceMapper;

    public VerificationHistoryPersistenceAdapter(
            VerificationHistoryJpaRepository verificationHistoryJpaRepository,
            VerificationHistoryPersistenceMapper verificationHistoryPersistenceMapper
    ) {
        this.verificationHistoryJpaRepository = verificationHistoryJpaRepository;
        this.verificationHistoryPersistenceMapper = verificationHistoryPersistenceMapper;
    }

    @Override
    public VerificationHistory save(VerificationHistory verificationHistory) {
        return verificationHistoryPersistenceMapper.toDomain(
                verificationHistoryJpaRepository.save(
                        verificationHistoryPersistenceMapper.toEntity(verificationHistory)
                )
        );
    }
}
