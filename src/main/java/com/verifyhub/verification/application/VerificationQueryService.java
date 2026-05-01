package com.verifyhub.verification.application;

import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VerificationQueryService {

    private final VerificationRepositoryPort verificationRepositoryPort;
    private final VerificationHistoryRepositoryPort verificationHistoryRepositoryPort;

    public VerificationQueryService(
            VerificationRepositoryPort verificationRepositoryPort,
            VerificationHistoryRepositoryPort verificationHistoryRepositoryPort
    ) {
        this.verificationRepositoryPort = verificationRepositoryPort;
        this.verificationHistoryRepositoryPort = verificationHistoryRepositoryPort;
    }

    public Verification getVerification(String verificationId) {
        return verificationRepositoryPort.findByVerificationId(verificationId)
                .orElseThrow(() -> new VerificationNotFoundException(verificationId));
    }

    public List<VerificationHistory> getHistories(String verificationId) {
        getVerification(verificationId);
        return verificationHistoryRepositoryPort.findByVerificationIdOrderByCreatedAtAsc(verificationId);
    }
}
