package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.VerificationHistory;
import java.util.List;

public interface VerificationHistoryRepositoryPort {

    VerificationHistory save(VerificationHistory verificationHistory);

    List<VerificationHistory> findByVerificationIdOrderByCreatedAtAsc(String verificationId);
}
