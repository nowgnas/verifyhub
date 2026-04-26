package com.verifyhub.routing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.verifyhub.common.exception.ProviderUnavailableException;
import com.verifyhub.verification.domain.ProviderType;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeightedProviderRoutingStrategyTest {

    @Test
    void selectsProviderByWeightRange() {
        WeightedProviderRoutingStrategy strategy = new WeightedProviderRoutingStrategy(bound -> 95);

        RoutingDecision decision = strategy.select(List.of(
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 1L),
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L)
        ));

        assertThat(decision.selectedProvider()).contains(ProviderType.NICE);
        assertThat(decision.reason()).isEqualTo(RoutingReason.WEIGHTED_SELECTED);
        assertThat(decision.policyVersion()).isEqualTo(1L);
        assertThat(decision.candidateProviders()).containsExactly(ProviderType.KG, ProviderType.NICE);
    }

    @Test
    void canSelectLowWeightProviderWhenRandomFallsInFirstRange() {
        WeightedProviderRoutingStrategy strategy = new WeightedProviderRoutingStrategy(bound -> 0);

        RoutingDecision decision = strategy.select(List.of(
                new ProviderRoutingPolicy(ProviderType.KG, 10, true, 1L),
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L)
        ));

        assertThat(decision.selectedProvider()).contains(ProviderType.KG);
    }

    @Test
    void rejectsEmptyCandidates() {
        WeightedProviderRoutingStrategy strategy = new WeightedProviderRoutingStrategy(bound -> 0);

        assertThatThrownBy(() -> strategy.select(List.of()))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void rejectsCandidatesWhenTotalWeightIsZero() {
        WeightedProviderRoutingStrategy strategy = new WeightedProviderRoutingStrategy(bound -> 0);

        assertThatThrownBy(() -> strategy.select(List.of(
                        new ProviderRoutingPolicy(ProviderType.KG, 0, true, 1L),
                        new ProviderRoutingPolicy(ProviderType.NICE, 0, true, 1L)
                )))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void skipsZeroWeightProviderRange() {
        WeightedProviderRoutingStrategy strategy = new WeightedProviderRoutingStrategy(bound -> 0);

        RoutingDecision decision = strategy.select(List.of(
                new ProviderRoutingPolicy(ProviderType.KG, 0, true, 1L),
                new ProviderRoutingPolicy(ProviderType.NICE, 90, true, 1L)
        ));

        assertThat(decision.selectedProvider()).contains(ProviderType.NICE);
        assertThat(decision.candidateProviders()).containsExactly(ProviderType.KG, ProviderType.NICE);
    }
}
