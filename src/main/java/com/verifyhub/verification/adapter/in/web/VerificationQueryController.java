package com.verifyhub.verification.adapter.in.web;

import com.verifyhub.common.response.ApiResponse;
import com.verifyhub.verification.adapter.in.web.dto.VerificationHistoryListResponse;
import com.verifyhub.verification.adapter.in.web.dto.VerificationQueryResponse;
import com.verifyhub.verification.application.VerificationQueryService;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationHistory;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
public class VerificationQueryController {

    private final VerificationQueryService verificationQueryService;

    public VerificationQueryController(VerificationQueryService verificationQueryService) {
        this.verificationQueryService = verificationQueryService;
    }

    @GetMapping("/{verificationId}")
    public ApiResponse<VerificationQueryResponse> getVerification(@PathVariable String verificationId) {
        Verification verification = verificationQueryService.getVerification(verificationId);
        return ApiResponse.success(VerificationQueryResponse.from(verification));
    }

    @GetMapping("/{verificationId}/histories")
    public ApiResponse<VerificationHistoryListResponse> getHistories(@PathVariable String verificationId) {
        List<VerificationHistory> histories = verificationQueryService.getHistories(verificationId);
        return ApiResponse.success(VerificationHistoryListResponse.from(verificationId, histories));
    }
}
