package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import java.util.Optional;

public interface VerificationRepositoryPort {

    Verification save(Verification verification);

    Optional<Verification> findByVerificationId(String verificationId);

    Optional<Verification> findByRequestIdAndPurposeAndIdempotencyKey(
            String requestId,
            VerificationPurpose purpose,
            String idempotencyKey
    );
}
