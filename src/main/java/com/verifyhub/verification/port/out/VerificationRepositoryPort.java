package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import java.util.Optional;

public interface VerificationRepositoryPort {

    Verification save(Verification verification);

    Optional<Verification> findByVerificationId(String verificationId);

    Optional<Verification> findByUserIdAndPurposeAndIdempotencyKey(
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey
    );
}
