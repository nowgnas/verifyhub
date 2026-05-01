package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;

public record VerificationHistoryResponse(
        VerificationStatus fromStatus,
        VerificationStatus toStatus,
        VerificationEvent eventType,
        String reason,
        ProviderType provider,
        LocalDateTime createdAt
) {

    public static VerificationHistoryResponse from(VerificationHistory history) {
        return new VerificationHistoryResponse(
                history.fromStatus(),
                history.toStatus(),
                history.eventType(),
                history.reason(),
                history.provider(),
                history.createdAt()
        );
    }
}
