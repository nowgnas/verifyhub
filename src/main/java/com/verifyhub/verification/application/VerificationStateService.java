package com.verifyhub.verification.application;

import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationHistory;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.VerificationHistoryRepositoryPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationStateService {

    private final VerificationRepositoryPort verificationRepositoryPort;
    private final VerificationHistoryRepositoryPort verificationHistoryRepositoryPort;

    public VerificationStateService(
            VerificationRepositoryPort verificationRepositoryPort,
            VerificationHistoryRepositoryPort verificationHistoryRepositoryPort
    ) {
        this.verificationRepositoryPort = verificationRepositoryPort;
        this.verificationHistoryRepositoryPort = verificationHistoryRepositoryPort;
    }

    @Transactional
    public Verification routeTo(
            String verificationId,
            ProviderType provider,
            Long routingPolicyVersion,
            LocalDateTime routedAt
    ) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.routeTo(provider, routingPolicyVersion, routedAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.ROUTE_SELECTED, null, null);
    }

    @Transactional
    public Verification startProviderCall(
            String verificationId,
            String providerTransactionId,
            String providerRequestNo,
            LocalDateTime providerCalledAt
    ) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.startProviderCall(providerTransactionId, providerRequestNo, providerCalledAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.PROVIDER_CALL_STARTED, null, null);
    }

    @Transactional
    public Verification markSuccess(String verificationId, LocalDateTime completedAt) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.succeed(completedAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.PROVIDER_CALL_SUCCEEDED, null, null);
    }

    @Transactional
    public Verification markFail(String verificationId, LocalDateTime completedAt, String reason) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.fail(completedAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.PROVIDER_CALL_FAILED, reason, null);
    }

    @Transactional
    public Verification markTimeout(String verificationId, LocalDateTime completedAt, String reason) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.timeout(completedAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.PROVIDER_TIMEOUT, reason, null);
    }

    @Transactional
    public Verification cancel(String verificationId, LocalDateTime completedAt, String reason) {
        Verification verification = findByVerificationId(verificationId);
        VerificationStatus fromStatus = verification.getStatus();
        verification.cancel(completedAt);
        return saveWithHistory(verification, fromStatus, VerificationEvent.CANCEL_REQUESTED, reason, null);
    }

    private Verification findByVerificationId(String verificationId) {
        return verificationRepositoryPort.findByVerificationId(verificationId)
                .orElseThrow(() -> new VerificationNotFoundException(verificationId));
    }

    private Verification saveWithHistory(
            Verification verification,
            VerificationStatus fromStatus,
            VerificationEvent eventType,
            String reason,
            String rawPayload
    ) {
        Verification saved = verificationRepositoryPort.save(verification);
        verificationHistoryRepositoryPort.save(VerificationHistory.of(
                saved.getVerificationId(),
                fromStatus,
                saved.getStatus(),
                eventType,
                reason,
                saved.getProvider(),
                rawPayload,
                LocalDateTime.now()
        ));
        return saved;
    }
}
