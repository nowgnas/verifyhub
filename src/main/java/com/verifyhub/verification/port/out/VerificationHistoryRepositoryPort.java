package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.VerificationHistory;

public interface VerificationHistoryRepositoryPort {

    VerificationHistory save(VerificationHistory verificationHistory);
}
