package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationHistoryJpaRepository extends JpaRepository<VerificationHistoryEntity, Long> {

    List<VerificationHistoryEntity> findByVerificationIdOrderByCreatedAtAsc(String verificationId);
}
