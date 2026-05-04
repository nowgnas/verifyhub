package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.application.ProviderReturnResult;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationStatus;

public record ProviderReturnResponse(
        String verificationId,
        ProviderType provider,
        VerificationStatus status,
        ProviderResultStatus result,
        boolean integrityVerified
) {

    public static ProviderReturnResponse from(ProviderReturnResult result) {
        return new ProviderReturnResponse(
                result.verificationId(),
                result.provider(),
                result.status(),
                result.result(),
                result.integrityVerified()
        );
    }
}
