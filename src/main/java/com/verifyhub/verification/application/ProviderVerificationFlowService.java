package com.verifyhub.verification.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verifyhub.common.exception.ProviderUnavailableException;
import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.routing.application.ProviderRoutingService;
import com.verifyhub.routing.domain.RoutingDecision;
import com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecorator;
import com.verifyhub.verification.domain.ProviderCallResultType;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderRequestResultType;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.Verification;
import com.verifyhub.verification.port.out.ProviderClientPort;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderVerificationFlowService {

    private final ProviderRoutingService providerRoutingService;
    private final VerificationStateService verificationStateService;
    private final ProviderCallHistoryService providerCallHistoryService;
    private final Map<ProviderType, ProviderClientPort> providerClients;
    private final ProviderClientResilienceDecorator resilienceDecorator;
    private final TimeProvider timeProvider;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProviderVerificationFlowService(
            ProviderRoutingService providerRoutingService,
            VerificationStateService verificationStateService,
            ProviderCallHistoryService providerCallHistoryService,
            List<ProviderClientPort> providerClients,
            ProviderClientResilienceDecorator resilienceDecorator,
            TimeProvider timeProvider
    ) {
        this(
                providerRoutingService,
                verificationStateService,
                providerCallHistoryService,
                providerClients,
                resilienceDecorator,
                timeProvider,
                new ObjectMapper()
        );
    }

    ProviderVerificationFlowService(
            ProviderRoutingService providerRoutingService,
            VerificationStateService verificationStateService,
            ProviderCallHistoryService providerCallHistoryService,
            List<ProviderClientPort> providerClients,
            ProviderClientResilienceDecorator resilienceDecorator,
            TimeProvider timeProvider,
            ObjectMapper objectMapper
    ) {
        this.providerRoutingService = providerRoutingService;
        this.verificationStateService = verificationStateService;
        this.providerCallHistoryService = providerCallHistoryService;
        this.providerClients = toProviderClientMap(providerClients);
        this.resilienceDecorator = resilienceDecorator;
        this.timeProvider = timeProvider;
        this.objectMapper = objectMapper;
    }

    public ProviderVerificationResult requestProviderVerification(ProviderVerificationCommand command) {
        Verification verification = command.verification();
        RoutingDecision routingDecision = providerRoutingService.route(command.providerHealthSnapshots());
        ProviderType provider = routingDecision.selectedProvider()
                .orElseThrow(ProviderUnavailableException::new);
        ProviderClientPort providerClient = providerClient(provider);
        LocalDateTime now = timeProvider.now();

        Verification routed = verificationStateService.routeTo(
                verification.getVerificationId(),
                provider,
                routingDecision.policyVersion(),
                now
        );

        ProviderRequest providerRequest = new ProviderRequest(
                routed.getVerificationId(),
                routed.getRequestId(),
                command.name(),
                command.phoneNumber(),
                command.birthDate(),
                routed.getPurpose().name()
        );
        ProviderRequestResult providerResult = resilienceDecorator.requestVerification(providerClient, providerRequest);
        recordProviderCall(routed.getVerificationId(), provider, providerRequest, providerResult);

        Verification inProgress = verificationStateService.startProviderCall(
                routed.getVerificationId(),
                providerResult.providerTransactionId(),
                providerResult.providerRequestNo(),
                now
        );
        Verification completed = completeIfTerminalProviderResult(inProgress, providerResult, now);

        return new ProviderVerificationResult(
                completed.getVerificationId(),
                provider,
                completed.getStatus(),
                providerResult.authUrl()
        );
    }

    private Verification completeIfTerminalProviderResult(
            Verification verification,
            ProviderRequestResult providerResult,
            LocalDateTime completedAt
    ) {
        if (providerResult.resultType() == ProviderRequestResultType.SUCCESS) {
            return verificationStateService.markSuccess(verification.getVerificationId(), completedAt);
        }
        if (providerResult.resultType() == ProviderRequestResultType.FAIL) {
            return verificationStateService.markFail(verification.getVerificationId(), completedAt, providerResult.errorMessage());
        }
        if (providerResult.resultType() == ProviderRequestResultType.TIMEOUT) {
            return verificationStateService.markTimeout(verification.getVerificationId(), completedAt, providerResult.errorMessage());
        }
        return verification;
    }

    private void recordProviderCall(
            String verificationId,
            ProviderType provider,
            ProviderRequest providerRequest,
            ProviderRequestResult providerResult
    ) {
        providerCallHistoryService.record(
                verificationId,
                provider,
                toJson(providerRequest),
                providerResult.rawResponse(),
                providerResult.httpStatus(),
                toProviderCallResultType(providerResult.resultType()),
                providerResult.latencyMs(),
                providerResult.errorMessage(),
                0
        );
    }

    private ProviderCallResultType toProviderCallResultType(ProviderRequestResultType resultType) {
        return ProviderCallResultType.valueOf(resultType.name());
    }

    private ProviderClientPort providerClient(ProviderType provider) {
        ProviderClientPort providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new ProviderUnavailableException();
        }
        return providerClient;
    }

    private Map<ProviderType, ProviderClientPort> toProviderClientMap(List<ProviderClientPort> providerClients) {
        Map<ProviderType, ProviderClientPort> result = new EnumMap<>(ProviderType.class);
        for (ProviderClientPort providerClient : providerClients) {
            result.put(providerClient.providerType(), providerClient);
        }
        return Map.copyOf(result);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return value.toString();
        }
    }
}
