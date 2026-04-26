package com.verifyhub.verification.adapter.out.outbox;

import com.verifyhub.verification.adapter.out.persistence.mapper.OutboxEventPersistenceMapper;
import com.verifyhub.verification.adapter.out.persistence.repository.OutboxEventJpaRepository;
import com.verifyhub.verification.domain.OutboxEvent;
import com.verifyhub.verification.port.out.OutboxEventPort;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxEventPersistenceMapper outboxEventPersistenceMapper;

    public OutboxEventPersistenceAdapter(
            OutboxEventJpaRepository outboxEventJpaRepository,
            OutboxEventPersistenceMapper outboxEventPersistenceMapper
    ) {
        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.outboxEventPersistenceMapper = outboxEventPersistenceMapper;
    }

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return outboxEventPersistenceMapper.toDomain(
                outboxEventJpaRepository.save(outboxEventPersistenceMapper.toEntity(outboxEvent))
        );
    }
}
