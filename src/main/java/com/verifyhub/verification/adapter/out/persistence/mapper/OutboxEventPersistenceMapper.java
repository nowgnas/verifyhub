package com.verifyhub.verification.adapter.out.persistence.mapper;

import com.verifyhub.verification.adapter.out.persistence.entity.OutboxEventEntity;
import com.verifyhub.verification.domain.OutboxEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OutboxEventPersistenceMapper {

    OutboxEventEntity toEntity(OutboxEvent outboxEvent);

    OutboxEvent toDomain(OutboxEventEntity outboxEventEntity);
}
