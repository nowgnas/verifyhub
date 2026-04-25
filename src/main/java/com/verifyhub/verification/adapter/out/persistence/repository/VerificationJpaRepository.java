package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationEntity;
import com.verifyhub.verification.domain.VerificationPurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationJpaRepository extends JpaRepository<VerificationEntity, Long> {

    Optional<VerificationEntity> findByVerificationId(String verificationId);

    Optional<VerificationEntity> findByUserIdAndPurposeAndIdempotencyKey(
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey
    );
}
