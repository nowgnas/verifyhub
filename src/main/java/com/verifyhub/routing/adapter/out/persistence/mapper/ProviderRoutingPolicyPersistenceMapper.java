package com.verifyhub.routing.adapter.out.persistence.mapper;

import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.verification.adapter.out.persistence.entity.ProviderRoutingPolicyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProviderRoutingPolicyPersistenceMapper {

    ProviderRoutingPolicy toDomain(ProviderRoutingPolicyEntity entity);
}
