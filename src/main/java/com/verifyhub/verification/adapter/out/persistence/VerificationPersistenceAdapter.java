package com.verifyhub.verification.adapter.out.persistence;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationEntity;
import com.verifyhub.verification.adapter.out.persistence.mapper.VerificationPersistenceMapper;
import com.verifyhub.verification.adapter.out.persistence.repository.VerificationJpaRepository;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class VerificationPersistenceAdapter implements VerificationRepositoryPort {

    private final VerificationJpaRepository verificationJpaRepository;
    private final VerificationPersistenceMapper verificationPersistenceMapper;

    public VerificationPersistenceAdapter(
            VerificationJpaRepository verificationJpaRepository,
            VerificationPersistenceMapper verificationPersistenceMapper
    ) {
        this.verificationJpaRepository = verificationJpaRepository;
        this.verificationPersistenceMapper = verificationPersistenceMapper;
    }

    @Override
    public Verification save(Verification verification) {
        LocalDateTime now = LocalDateTime.now();
        VerificationEntity existing = verification.getId() == null
                ? null
                : verificationJpaRepository.findById(verification.getId()).orElse(null);
        LocalDateTime createdAt = existing == null ? now : existing.getCreatedAt();
        LocalDateTime updatedAt = now;
        VerificationEntity saved = verificationJpaRepository.save(
                verificationPersistenceMapper.toEntity(
                        verification,
                        new VerificationPersistenceMapper.AuditFields(createdAt, updatedAt)
                )
        );
        return verificationPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Verification> findByVerificationId(String verificationId) {
        return verificationJpaRepository.findByVerificationId(verificationId)
                .map(verificationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Verification> findByUserIdAndPurposeAndIdempotencyKey(
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey
    ) {
        return verificationJpaRepository.findByUserIdAndPurposeAndIdempotencyKey(userId, purpose, idempotencyKey)
                .map(verificationPersistenceMapper::toDomain);
    }
}
