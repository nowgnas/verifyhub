package com.verifyhub.routing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.verifyhub.routing.domain.ProviderHealthSnapshot;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import com.verifyhub.routing.domain.RoutingDecision;
import com.verifyhub.routing.domain.RoutingReason;
import com.verifyhub.routing.domain.WeightedProviderRoutingStrategy;
import com.verifyhub.routing.port.out.ProviderRoutingPolicyRepositoryPort;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderRoutingServiceTest {

    private final ProviderRoutingPolicyRepositoryPort providerRoutingPolicyRepositoryPort =
            mock(ProviderRoutingPolicyRepositoryPort.class);
    private final ProviderRoutingService providerRoutingService =
            new ProviderRoutingService(providerRoutingPolicyRepositoryPort, new WeightedProviderRoutingStrategy(bound -> bound - 1));

    @Test
    void preparesRoutingDecisionFromLatestEnabledPolicies() {
        when(providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies()).thenReturn(List.of(
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L),
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 1L)
        ));

        RoutingDecision decision = providerRoutingService.route(List.of(
                ProviderHealthSnapshot.available(ProviderType.NICE),
                ProviderHealthSnapshot.available(ProviderType.KG)
        ));

        assertThat(decision.reason()).isEqualTo(RoutingReason.WEIGHTED_SELECTED);
        assertThat(decision.policyVersion()).isEqualTo(1L);
        assertThat(decision.candidateProviders()).containsExactly(ProviderType.NICE, ProviderType.KG);
        assertThat(decision.selectedProvider()).contains(ProviderType.KG);
    }

    @Test
    void returnsNoEnabledPolicyDecisionWhenLatestEnabledPoliciesAreEmpty() {
        when(providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies()).thenReturn(List.of());

        RoutingDecision decision = providerRoutingService.route(List.of());

        assertThat(decision.reason()).isEqualTo(RoutingReason.NO_ENABLED_POLICY);
        assertThat(decision.policyVersion()).isNull();
        assertThat(decision.candidateProviders()).isEmpty();
        assertThat(decision.selectedProvider()).isEmpty();
    }

    @Test
    void excludesUnavailableProvidersFromCandidates() {
        when(providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies()).thenReturn(List.of(
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L),
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 1L)
        ));

        RoutingDecision decision = providerRoutingService.route(List.of(
                ProviderHealthSnapshot.unavailable(ProviderType.NICE, "circuit open"),
                ProviderHealthSnapshot.available(ProviderType.KG)
        ));

        assertThat(decision.reason()).isEqualTo(RoutingReason.WEIGHTED_SELECTED);
        assertThat(decision.candidateProviders()).containsExactly(ProviderType.KG);
        assertThat(decision.selectedProvider()).contains(ProviderType.KG);
    }

    @Test
    void returnsNoAvailableProviderDecisionWhenAllPolicyProvidersAreUnavailable() {
        when(providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies()).thenReturn(List.of(
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 2L),
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 2L)
        ));

        RoutingDecision decision = providerRoutingService.route(List.of(
                ProviderHealthSnapshot.unavailable(ProviderType.NICE, "circuit open"),
                ProviderHealthSnapshot.unavailable(ProviderType.KG, "timeout")
        ));

        assertThat(decision.reason()).isEqualTo(RoutingReason.NO_AVAILABLE_PROVIDER);
        assertThat(decision.policyVersion()).isEqualTo(2L);
        assertThat(decision.candidateProviders()).isEmpty();
        assertThat(decision.selectedProvider()).isEmpty();
    }

    @Test
    void treatsMissingHealthSnapshotAsAvailable() {
        when(providerRoutingPolicyRepositoryPort.findLatestEnabledPolicies()).thenReturn(List.of(
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L),
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 1L)
        ));

        RoutingDecision decision = providerRoutingService.route(List.of(
                ProviderHealthSnapshot.unavailable(ProviderType.NICE, "circuit open")
        ));

        assertThat(decision.reason()).isEqualTo(RoutingReason.WEIGHTED_SELECTED);
        assertThat(decision.candidateProviders()).containsExactly(ProviderType.KG);
        assertThat(decision.selectedProvider()).contains(ProviderType.KG);
    }
}
