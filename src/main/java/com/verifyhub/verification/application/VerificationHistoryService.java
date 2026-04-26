package com.verifyhub.verification.application;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class VerificationHistoryService {

    private final VerificationHistoryRepositoryPort verificationHistoryRepositoryPort;
    private final TimeProvider timeProvider;

    public VerificationHistoryService(
            VerificationHistoryRepositoryPort verificationHistoryRepositoryPort,
            TimeProvider timeProvider
    ) {
        this.verificationHistoryRepositoryPort = verificationHistoryRepositoryPort;
        this.timeProvider = timeProvider;
    }

    public VerificationHistory record(
            String verificationId,
            VerificationStatus fromStatus,
            VerificationStatus toStatus,
            VerificationEvent eventType,
            String reason,
            ProviderType provider,
            String rawPayload
    ) {
        return verificationHistoryRepositoryPort.save(VerificationHistory.of(
                verificationId,
                fromStatus,
                toStatus,
                eventType,
                reason,
                provider,
                rawPayload,
                timeProvider.now()
        ));
    }
}
