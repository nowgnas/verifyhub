package com.verifyhub.verification.adapter.in.web;

import com.verifyhub.common.response.ApiResponse;
import com.verifyhub.verification.adapter.in.web.dto.ProviderReturnRequest;
import com.verifyhub.verification.adapter.in.web.dto.ProviderReturnResponse;
import com.verifyhub.verification.application.ProviderReturnCommand;
import com.verifyhub.verification.application.ProviderReturnResult;
import com.verifyhub.verification.application.ProviderReturnService;
import com.verifyhub.verification.domain.ProviderType;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/providers/{provider}/returns")
public class ProviderReturnController {

    private final ProviderReturnService providerReturnService;

    public ProviderReturnController(ProviderReturnService providerReturnService) {
        this.providerReturnService = providerReturnService;
    }

    @GetMapping
    public ApiResponse<ProviderReturnResponse> handleGetReturn(
            @PathVariable ProviderType provider,
            @RequestParam @NotBlank String verificationId,
            @RequestParam @NotBlank String webTransactionId
    ) {
        ProviderReturnResult result = providerReturnService.handleReturn(new ProviderReturnCommand(
                provider,
                verificationId,
                webTransactionId
        ));
        return ApiResponse.success(ProviderReturnResponse.from(result));
    }

    @PostMapping
    public ApiResponse<ProviderReturnResponse> handlePostReturn(
            @PathVariable ProviderType provider,
            @Valid @RequestBody ProviderReturnRequest request
    ) {
        ProviderReturnResult result = providerReturnService.handleReturn(new ProviderReturnCommand(
                provider,
                request.verificationId(),
                request.webTransactionId()
        ));
        return ApiResponse.success(ProviderReturnResponse.from(result));
    }
}
