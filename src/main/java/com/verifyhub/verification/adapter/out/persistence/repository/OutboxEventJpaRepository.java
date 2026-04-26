package com.verifyhub.verification.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
}
