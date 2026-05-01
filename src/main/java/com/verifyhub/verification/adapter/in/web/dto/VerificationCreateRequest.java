package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.application.VerificationCreateCommand;
import com.verifyhub.verification.domain.VerificationPurpose;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record VerificationCreateRequest(
        @NotBlank String requestId,
        @NotNull VerificationPurpose purpose,
        @NotBlank String returnUrl,
        String closeUrl,
        @NotEmpty List<@NotBlank String> svcTypes
) {

    public VerificationCreateCommand toCommand(String idempotencyKey) {
        return new VerificationCreateCommand(
                requestId,
                purpose,
                idempotencyKey,
                returnUrl,
                closeUrl,
                svcTypes
        );
    }
}
