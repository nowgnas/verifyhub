package com.verifyhub.verification.application;

import com.verifyhub.common.exception.InvalidRequestException;
import com.verifyhub.common.exception.ProviderUnavailableException;
import com.verifyhub.common.exception.VerificationNotFoundException;
import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecorator;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderResultStatus;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.domain.VerificationEvent;
import com.verifyhub.verification.domain.VerificationStatus;
import com.verifyhub.verification.port.out.ProviderClientPort;
import com.verifyhub.verification.port.out.VerificationRepositoryPort;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProviderReturnService {

    private final VerificationRepositoryPort verificationRepositoryPort;
    private final VerificationStateService verificationStateService;
    private final Map<ProviderType, ProviderClientPort> providerClients;
    private final ProviderClientResilienceDecorator resilienceDecorator;
    private final OutboxEventService outboxEventService;
    private final TimeProvider timeProvider;
    private final LateCallbackHistoryService lateCallbackHistoryService;

    public ProviderReturnService(
            VerificationRepositoryPort verificationRepositoryPort,
            VerificationStateService verificationStateService,
            List<ProviderClientPort> providerClients,
            ProviderClientResilienceDecorator resilienceDecorator,
            OutboxEventService outboxEventService,
            TimeProvider timeProvider,
            LateCallbackHistoryService lateCallbackHistoryService
    ) {
        this.verificationRepositoryPort = verificationRepositoryPort;
        this.verificationStateService = verificationStateService;
        this.providerClients = toProviderClientMap(providerClients);
        this.resilienceDecorator = resilienceDecorator;
        this.outboxEventService = outboxEventService;
        this.timeProvider = timeProvider;
        this.lateCallbackHistoryService = lateCallbackHistoryService;
    }

    public ProviderReturnResult handleReturn(ProviderReturnCommand command) {
        Verification verification = verificationRepositoryPort.findByVerificationId(command.verificationId())
                .orElseThrow(() -> new VerificationNotFoundException(command.verificationId()));
        validateProvider(command.provider(), verification);

        ProviderClientPort providerClient = providerClient(command.provider());
        ProviderResult providerResult = resilienceDecorator.requestResult(providerClient, new ProviderResultRequest(
                command.provider(),
                verification.getProviderTransactionId(),
                verification.getProviderRequestNo(),
                verification.getVerificationId(),
                command.webTransactionId()
        ));

        if (verification.getStatus() != VerificationStatus.IN_PROGRESS) {
            return recordLateCallback(command, verification, providerResult);
        }

        boolean success = providerResult.result() == ProviderResultStatus.SUCCESS && providerResult.integrityVerified();
        verificationStateService.recordProviderReturn(
                verification.getVerificationId(),
                command.webTransactionId(),
                success ? VerificationEvent.CALLBACK_SUCCESS : VerificationEvent.CALLBACK_FAIL,
                providerResult.rawPayload()
        );

        Verification completed;
        LocalDateTime completedAt = timeProvider.now();
        if (success) {
            completed = verificationStateService.markSuccess(verification.getVerificationId(), completedAt);
            outboxEventService.enqueue("Verification", verification.getVerificationId(), "VERIFICATION_SUCCEEDED", outboxPayload(command.provider(), providerResult.result()));
        } else {
            completed = verificationStateService.markFail(verification.getVerificationId(), completedAt, failReason(providerResult));
            outboxEventService.enqueue("Verification", verification.getVerificationId(), "VERIFICATION_FAILED", outboxPayload(command.provider(), providerResult.result()));
        }

        return new ProviderReturnResult(
                completed.getVerificationId(),
                completed.getProvider(),
                completed.getStatus(),
                providerResult.result(),
                providerResult.integrityVerified()
        );
    }

    private void validateProvider(ProviderType pathProvider, Verification verification) {
        if (verification.getProvider() != pathProvider) {
            throw new InvalidRequestException("provider mismatch: path provider " + pathProvider + " does not match verification provider " + verification.getProvider());
        }
    }

    private ProviderReturnResult recordLateCallback(
            ProviderReturnCommand command,
            Verification verification,
            ProviderResult providerResult
    ) {
        boolean duplicate = command.webTransactionId().equals(verification.getWebTransactionId());
        lateCallbackHistoryService.record(
                verification.getVerificationId(),
                command.provider(),
                verification.getStatus(),
                providerResult.result().name(),
                duplicate,
                providerResult.rawPayload(),
                duplicate
                        ? "duplicate provider return received after terminal status"
                        : "late provider return received after terminal status"
        );
        return new ProviderReturnResult(
                verification.getVerificationId(),
                verification.getProvider(),
                verification.getStatus(),
                providerResult.result(),
                providerResult.integrityVerified()
        );
    }

    private ProviderClientPort providerClient(ProviderType provider) {
        ProviderClientPort providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new ProviderUnavailableException();
        }
        return providerClient;
    }

    private String failReason(ProviderResult providerResult) {
        if (!providerResult.integrityVerified()) {
            return "provider result integrity verification failed";
        }
        return "provider result failed";
    }

    private String outboxPayload(ProviderType provider, ProviderResultStatus result) {
        return "{\"provider\":\"" + provider.name() + "\",\"result\":\"" + result.name() + "\"}";
    }

    private Map<ProviderType, ProviderClientPort> toProviderClientMap(List<ProviderClientPort> providerClients) {
        Map<ProviderType, ProviderClientPort> result = new EnumMap<>(ProviderType.class);
        for (ProviderClientPort providerClient : providerClients) {
            result.put(providerClient.providerType(), providerClient);
        }
        return Map.copyOf(result);
    }
}
