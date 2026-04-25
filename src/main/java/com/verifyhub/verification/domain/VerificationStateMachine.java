package com.verifyhub.verification.domain;

import com.verifyhub.common.exception.InvalidStateTransitionException;
import java.util.Objects;

public final class VerificationStateMachine {

    private VerificationStateMachine() {
    }

    public static VerificationStatus transit(VerificationStatus current, VerificationEvent event) {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(event, "event must not be null");

        if (current.isTerminal()) {
            throw invalidTransition(current, event);
        }

        if (event == VerificationEvent.CANCEL_REQUESTED) {
            return VerificationStatus.CANCELED;
        }

        if (current == VerificationStatus.REQUESTED && event == VerificationEvent.ROUTE_SELECTED) {
            return VerificationStatus.ROUTED;
        }

        if (current == VerificationStatus.ROUTED && event == VerificationEvent.PROVIDER_CALL_STARTED) {
            return VerificationStatus.IN_PROGRESS;
        }

        if (current == VerificationStatus.IN_PROGRESS) {
            return switch (event) {
                case PROVIDER_CALL_SUCCEEDED, CALLBACK_SUCCESS -> VerificationStatus.SUCCESS;
                case PROVIDER_CALL_FAILED, CALLBACK_FAIL -> VerificationStatus.FAIL;
                case PROVIDER_TIMEOUT -> VerificationStatus.TIMEOUT;
                default -> throw invalidTransition(current, event);
            };
        }

        throw invalidTransition(current, event);
    }

    private static InvalidStateTransitionException invalidTransition(VerificationStatus current, VerificationEvent event) {
        return new InvalidStateTransitionException("Invalid verification state transition: " + current + " + " + event);
    }
}
