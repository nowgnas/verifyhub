package com.verifyhub.verification.adapter.in.web;

import com.verifyhub.common.exception.InvalidRequestException;
import com.verifyhub.common.response.ApiResponse;
import com.verifyhub.verification.adapter.in.web.dto.VerificationCreateRequest;
import com.verifyhub.verification.adapter.in.web.dto.VerificationCreateResponse;
import com.verifyhub.verification.application.ProviderVerificationResult;
import com.verifyhub.verification.application.VerificationCreateService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
public class VerificationCreateController {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final VerificationCreateService verificationCreateService;

    public VerificationCreateController(VerificationCreateService verificationCreateService) {
        this.verificationCreateService = verificationCreateService;
    }

    @PostMapping
    public ApiResponse<VerificationCreateResponse> create(
            @RequestHeader(value = IDEMPOTENCY_KEY, required = false) String idempotencyKey,
            @Valid @RequestBody VerificationCreateRequest request
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidRequestException("Idempotency-Key header is required");
        }
        ProviderVerificationResult result = verificationCreateService.create(request.toCommand(idempotencyKey));
        return ApiResponse.success(VerificationCreateResponse.from(result));
    }
}
