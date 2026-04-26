package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.ProviderCallHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderCallHistoryJpaRepository extends JpaRepository<ProviderCallHistoryEntity, Long> {
}
