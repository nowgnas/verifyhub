package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.LateCallbackHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LateCallbackHistoryJpaRepository extends JpaRepository<LateCallbackHistoryEntity, Long> {
}
