package com.verifyhub.verification.adapter.in.web.dto;

import com.verifyhub.verification.domain.VerificationHistory;
import java.util.List;

public record VerificationHistoryListResponse(
        String verificationId,
        List<VerificationHistoryResponse> histories
) {

    public static VerificationHistoryListResponse from(String verificationId, List<VerificationHistory> histories) {
        return new VerificationHistoryListResponse(
                verificationId,
                histories.stream()
                        .map(VerificationHistoryResponse::from)
                        .toList()
        );
    }
}
