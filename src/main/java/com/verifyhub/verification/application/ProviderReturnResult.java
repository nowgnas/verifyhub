package com.verifyhub.verification.application;

import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;

public record ProviderReturnResult(
        String verificationId,
        ProviderType provider,
        VerificationStatus status,
        ProviderResultStatus result,
        boolean integrityVerified
) {
}
