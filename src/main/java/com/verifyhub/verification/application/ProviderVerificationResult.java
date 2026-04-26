package com.verifyhub.verification.application;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;

public record ProviderVerificationResult(
        String verificationId,
        ProviderType provider,
        VerificationStatus status,
        String authUrl
) {
}
