package com.verifyhub.routing.adapter.out.persistence.repository;

import com.verifyhub.verification.adapter.out.persistence.entity.ProviderRoutingPolicyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProviderRoutingPolicyJpaRepository extends JpaRepository<ProviderRoutingPolicyEntity, Long> {

    @Query("""
            select p
            from ProviderRoutingPolicyEntity p
            where p.enabled = true
              and p.version = (
                select max(p2.version)
                from ProviderRoutingPolicyEntity p2
              )
            order by p.weight desc, p.provider asc
            """)
    List<ProviderRoutingPolicyEntity> findLatestEnabledPolicies();
}
