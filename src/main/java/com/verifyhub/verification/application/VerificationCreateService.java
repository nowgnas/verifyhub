package com.verifyhub.verification.application;

import com.verifyhub.common.id.VerificationIdGenerator;
import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.idempotency.application.IdempotencyService;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationStatus;
import org.springframework.stereotype.Service;

@Service
public class VerificationCreateService {

    private final VerificationIdGenerator verificationIdGenerator;
    private final TimeProvider timeProvider;
    private final IdempotencyService idempotencyService;
    private final ProviderVerificationFlowService providerVerificationFlowService;

    public VerificationCreateService(
            VerificationIdGenerator verificationIdGenerator,
            TimeProvider timeProvider,
            IdempotencyService idempotencyService,
            ProviderVerificationFlowService providerVerificationFlowService
    ) {
        this.verificationIdGenerator = verificationIdGenerator;
        this.timeProvider = timeProvider;
        this.idempotencyService = idempotencyService;
        this.providerVerificationFlowService = providerVerificationFlowService;
    }

    public ProviderVerificationResult create(VerificationCreateCommand command) {
        Verification verification = idempotencyService.getOrCreate(
                command.requestId(),
                command.purpose(),
                command.idempotencyKey(),
                () -> Verification.requested(
                        verificationIdGenerator.generate(),
                        command.requestId(),
                        command.purpose(),
                        command.idempotencyKey(),
                        timeProvider.now()
                )
        );

        if (verification.getStatus() != VerificationStatus.REQUESTED) {
            return new ProviderVerificationResult(
                    verification.getVerificationId(),
                    verification.getProvider(),
                    verification.getStatus(),
                    null
            );
        }

        return providerVerificationFlowService.requestProviderVerification(new ProviderVerificationCommand(
                verification,
                command.returnUrl(),
                command.closeUrl(),
                command.svcTypes(),
                java.util.List.of()
        ));
    }
}
