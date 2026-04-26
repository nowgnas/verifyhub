package com.verifyhub.verification.adapter.out.persistence.mapper;

import com.verifyhub.verification.adapter.out.persistence.entity.VerificationEntity;
import com.verifyhub.verification.domain.Verification;
import java.time.LocalDateTime;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public abstract class VerificationPersistenceMapper {

    public abstract Verification toDomain(VerificationEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract VerificationEntity toEntity(Verification verification, @Context AuditFields auditFields);

    @AfterMapping
    protected void mapAuditFields(
            @MappingTarget VerificationEntity entity,
            @Context AuditFields auditFields
    ) {
        entity.setCreatedAt(auditFields.createdAt());
        entity.setUpdatedAt(auditFields.updatedAt());
    }

    @ObjectFactory
    protected Verification createVerification(VerificationEntity entity) {
        return Verification.rehydrate(
                entity.getId(),
                entity.getVerificationId(),
                entity.getUserId(),
                entity.getPurpose(),
                entity.getIdempotencyKey(),
                entity.getProvider(),
                entity.getStatus(),
                entity.getProviderTransactionId(),
                entity.getProviderRequestNo(),
                entity.getWebTransactionId(),
                entity.getRoutingPolicyVersion(),
                entity.getRequestedAt(),
                entity.getRoutedAt(),
                entity.getProviderCalledAt(),
                entity.getCompletedAt(),
                entity.getVersion()
        );
    }

    public record AuditFields(LocalDateTime createdAt, LocalDateTime updatedAt) {
    }
}
