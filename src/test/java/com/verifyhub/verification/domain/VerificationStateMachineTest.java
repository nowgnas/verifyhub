package com.verifyhub.verification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.verifyhub.common.exception.InvalidStateTransitionException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VerificationStateMachineTest {

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @MethodSource("allowedTransitions")
    void transitsAllowedState(VerificationStatus current, VerificationEvent event, VerificationStatus expected) {
        assertThat(VerificationStateMachine.transit(current, event)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} + {1}")
    @MethodSource("rejectedTransitions")
    void rejectsInvalidTransition(VerificationStatus current, VerificationEvent event) {
        assertThatThrownBy(() -> VerificationStateMachine.transit(current, event))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining(current.name())
                .hasMessageContaining(event.name());
    }

    @ParameterizedTest(name = "{0} + {1}")
    @MethodSource("terminalTransitions")
    @DisplayName("terminal status rejects every event")
    void rejectsEveryEventFromTerminalStatus(VerificationStatus current, VerificationEvent event) {
        assertThatThrownBy(() -> VerificationStateMachine.transit(current, event))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.ROUTE_SELECTED, VerificationStatus.ROUTED),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.PROVIDER_CALL_STARTED, VerificationStatus.IN_PROGRESS),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.PROVIDER_CALL_SUCCEEDED, VerificationStatus.SUCCESS),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.CALLBACK_SUCCESS, VerificationStatus.SUCCESS),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.PROVIDER_CALL_FAILED, VerificationStatus.FAIL),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.CALLBACK_FAIL, VerificationStatus.FAIL),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.PROVIDER_TIMEOUT, VerificationStatus.TIMEOUT),
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.CANCEL_REQUESTED, VerificationStatus.CANCELED),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.CANCEL_REQUESTED, VerificationStatus.CANCELED),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.CANCEL_REQUESTED, VerificationStatus.CANCELED)
        );
    }

    private static Stream<Arguments> rejectedTransitions() {
        return Stream.of(
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.VERIFICATION_REQUESTED),
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.PROVIDER_CALL_SUCCEEDED),
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.PROVIDER_CALL_FAILED),
                Arguments.of(VerificationStatus.REQUESTED, VerificationEvent.PROVIDER_TIMEOUT),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.ROUTE_SELECTED),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.PROVIDER_CALL_SUCCEEDED),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.PROVIDER_CALL_FAILED),
                Arguments.of(VerificationStatus.ROUTED, VerificationEvent.PROVIDER_TIMEOUT),
                Arguments.of(VerificationStatus.IN_PROGRESS, VerificationEvent.ROUTE_SELECTED)
        );
    }

    private static Stream<Arguments> terminalTransitions() {
        return Stream.of(VerificationStatus.SUCCESS, VerificationStatus.FAIL, VerificationStatus.TIMEOUT, VerificationStatus.CANCELED)
                .flatMap(status -> Stream.of(VerificationEvent.values()).map(event -> Arguments.of(status, event)));
    }
}
