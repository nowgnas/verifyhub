package com.verifyhub.verification.adapter.out.persistence.mapper;

import com.verifyhub.verification.adapter.out.persistence.entity.LateCallbackHistoryEntity;
import com.verifyhub.verification.domain.LateCallbackHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LateCallbackHistoryPersistenceMapper {

    LateCallbackHistoryEntity toEntity(LateCallbackHistory lateCallbackHistory);

    LateCallbackHistory toDomain(LateCallbackHistoryEntity lateCallbackHistoryEntity);
}
