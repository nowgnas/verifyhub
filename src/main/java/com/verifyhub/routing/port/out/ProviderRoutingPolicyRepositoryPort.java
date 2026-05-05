package com.verifyhub.routing.port.out;

import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import java.util.List;

public interface ProviderRoutingPolicyRepositoryPort {

    List<ProviderRoutingPolicy> findLatestEnabledPolicies();

    List<ProviderRoutingPolicy> findLatestPolicies();

    long findLatestVersion();

    List<ProviderRoutingPolicy> saveAll(List<ProviderRoutingPolicy> policies);
}
