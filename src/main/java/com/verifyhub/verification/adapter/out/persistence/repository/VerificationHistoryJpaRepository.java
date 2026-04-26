package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationHistoryJpaRepository extends JpaRepository<VerificationHistoryEntity, Long> {
}
