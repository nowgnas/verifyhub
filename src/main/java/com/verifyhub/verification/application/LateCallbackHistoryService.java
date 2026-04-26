package com.verifyhub.verification.application;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.LateCallbackHistory;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.LateCallbackHistoryRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class LateCallbackHistoryService {

    private final LateCallbackHistoryRepositoryPort lateCallbackHistoryRepositoryPort;
    private final TimeProvider timeProvider;

    public LateCallbackHistoryService(
            LateCallbackHistoryRepositoryPort lateCallbackHistoryRepositoryPort,
            TimeProvider timeProvider
    ) {
        this.lateCallbackHistoryRepositoryPort = lateCallbackHistoryRepositoryPort;
        this.timeProvider = timeProvider;
    }

    public LateCallbackHistory record(
            String verificationId,
            ProviderType provider,
            VerificationStatus currentStatus,
            String callbackResult,
            boolean duplicate,
            String rawPayload,
            String reason
    ) {
        return lateCallbackHistoryRepositoryPort.save(new LateCallbackHistory(
                null,
                verificationId,
                provider,
                currentStatus,
                callbackResult,
                duplicate,
                rawPayload,
                reason,
                timeProvider.now()
        ));
    }
}
