package com.verifyhub.verification.adapter.out.persistence.mapper;

import com.verifyhub.verification.adapter.out.persistence.entity.ProviderCallHistoryEntity;
import com.verifyhub.verification.domain.ProviderCallHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProviderCallHistoryPersistenceMapper {

    ProviderCallHistoryEntity toEntity(ProviderCallHistory providerCallHistory);

    ProviderCallHistory toDomain(ProviderCallHistoryEntity providerCallHistoryEntity);
}
