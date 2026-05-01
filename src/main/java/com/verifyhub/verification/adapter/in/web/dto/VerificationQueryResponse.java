package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;

public record VerificationQueryResponse(
        String verificationId,
        VerificationStatus status,
        ProviderType provider,
        VerificationPurpose purpose,
        LocalDateTime requestedAt,
        LocalDateTime completedAt
) {

    public static VerificationQueryResponse from(Verification verification) {
        return new VerificationQueryResponse(
                verification.getVerificationId(),
                verification.getStatus(),
                verification.getProvider(),
                verification.getPurpose(),
                verification.getRequestedAt(),
                verification.getCompletedAt()
        );
    }
}
