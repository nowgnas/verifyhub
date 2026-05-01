package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.application.ProviderVerificationResult;
import com.verifyhub.verification.domain.ProviderAuthEntry;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;

public record VerificationCreateResponse(
        String verificationId,
        VerificationStatus status,
        ProviderType provider,
        ProviderAuthEntry authEntry
) {

    public static VerificationCreateResponse from(ProviderVerificationResult result) {
        return new VerificationCreateResponse(
                result.verificationId(),
                result.status(),
                result.provider(),
                result.authEntry()
        );
    }
}
