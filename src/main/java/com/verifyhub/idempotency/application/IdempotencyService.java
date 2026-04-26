package com.verifyhub.idempotency.application;

import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.util.function.Supplier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final VerificationRepositoryPort verificationRepositoryPort;

    public IdempotencyService(VerificationRepositoryPort verificationRepositoryPort) {
        this.verificationRepositoryPort = verificationRepositoryPort;
    }

    @Transactional
    public Verification getOrCreate(
            String requestId,
            VerificationPurpose purpose,
            String idempotencyKey,
            Supplier<Verification> creator
    ) {
        return verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(requestId, purpose, idempotencyKey)
                .orElseGet(() -> createOrFindAfterConflict(requestId, purpose, idempotencyKey, creator));
    }

    private Verification createOrFindAfterConflict(
            String requestId,
            VerificationPurpose purpose,
            String idempotencyKey,
            Supplier<Verification> creator
    ) {
        try {
            return verificationRepositoryPort.save(creator.get());
        } catch (DataIntegrityViolationException exception) {
            return verificationRepositoryPort.findByRequestIdAndPurposeAndIdempotencyKey(requestId, purpose, idempotencyKey)
                    .orElseThrow(() -> exception);
        }
    }
}
