package com.verifyhub.verification.adapter.in.web.dto;

import javax.validation.constraints.NotBlank;

public record ProviderReturnRequest(
        @NotBlank String verificationId,
        @NotBlank String webTransactionId
) {
}
