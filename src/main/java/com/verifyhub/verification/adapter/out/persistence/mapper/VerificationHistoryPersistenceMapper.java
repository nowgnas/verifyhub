package com.verifyhub.verification.adapter.out.persistence.mapper;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationHistoryEntity;
import com.verifyhub.verification.domain.VerificationHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VerificationHistoryPersistenceMapper {

    VerificationHistoryEntity toEntity(VerificationHistory verificationHistory);

    VerificationHistory toDomain(VerificationHistoryEntity verificationHistoryEntity);
}
