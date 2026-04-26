package com.verifyhub.verification.application;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.ProviderCallHistory;
import com.verifyhub.verification.domain.ProviderCallResultType;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.port.out.ProviderCallHistoryRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ProviderCallHistoryService {

    private final ProviderCallHistoryRepositoryPort providerCallHistoryRepositoryPort;
    private final TimeProvider timeProvider;

    public ProviderCallHistoryService(
            ProviderCallHistoryRepositoryPort providerCallHistoryRepositoryPort,
            TimeProvider timeProvider
    ) {
        this.providerCallHistoryRepositoryPort = providerCallHistoryRepositoryPort;
        this.timeProvider = timeProvider;
    }

    public ProviderCallHistory record(
            String verificationId,
            ProviderType provider,
            String requestPayload,
            String responsePayload,
            Integer httpStatus,
            ProviderCallResultType resultType,
            Long latencyMs,
            String errorMessage,
            int retryCount
    ) {
        return providerCallHistoryRepositoryPort.save(new ProviderCallHistory(
                null,
                verificationId,
                provider,
                requestPayload,
                responsePayload,
                httpStatus,
                resultType,
                latencyMs,
                errorMessage,
                retryCount,
                timeProvider.now()
        ));
    }
}
