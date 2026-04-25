package com.verifyhub.verification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Verification {

    private final Long id;
    private final String verificationId;
    private final String userId;
    private final VerificationPurpose purpose;
    private final String idempotencyKey;
    private ProviderType provider;
    private VerificationStatus status;
    private String providerTransactionId;
    private String providerRequestNo;
    private String webTransactionId;
    private Long routingPolicyVersion;
    private final LocalDateTime requestedAt;
    private LocalDateTime routedAt;
    private LocalDateTime providerCalledAt;
    private LocalDateTime completedAt;
    private final Long version;

    private Verification(
            Long id,
            String verificationId,
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey,
            ProviderType provider,
            VerificationStatus status,
            String providerTransactionId,
            String providerRequestNo,
            String webTransactionId,
            Long routingPolicyVersion,
            LocalDateTime requestedAt,
            LocalDateTime routedAt,
            LocalDateTime providerCalledAt,
            LocalDateTime completedAt,
            Long version
    ) {
        this.id = id;
        this.verificationId = requireText(verificationId, "verificationId");
        this.userId = requireText(userId, "userId");
        this.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        this.provider = provider;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.providerTransactionId = providerTransactionId;
        this.providerRequestNo = providerRequestNo;
        this.webTransactionId = webTransactionId;
        this.routingPolicyVersion = routingPolicyVersion;
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        this.routedAt = routedAt;
        this.providerCalledAt = providerCalledAt;
        this.completedAt = completedAt;
        this.version = version;
    }

    public static Verification requested(
            String verificationId,
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey,
            LocalDateTime requestedAt
    ) {
        return new Verification(
                null,
                verificationId,
                userId,
                purpose,
                idempotencyKey,
                null,
                VerificationStatus.REQUESTED,
                null,
                null,
                null,
                null,
                requestedAt,
                null,
                null,
                null,
                0L
        );
    }

    public static Verification rehydrate(
            Long id,
            String verificationId,
            String userId,
            VerificationPurpose purpose,
            String idempotencyKey,
            ProviderType provider,
            VerificationStatus status,
            String providerTransactionId,
            String providerRequestNo,
            String webTransactionId,
            Long routingPolicyVersion,
            LocalDateTime requestedAt,
            LocalDateTime routedAt,
            LocalDateTime providerCalledAt,
            LocalDateTime completedAt,
            Long version
    ) {
        Verification verification = new Verification(
                id,
                verificationId,
                userId,
                purpose,
                idempotencyKey,
                provider,
                status,
                providerTransactionId,
                providerRequestNo,
                webTransactionId,
                routingPolicyVersion,
                requestedAt,
                routedAt,
                providerCalledAt,
                completedAt,
                version
        );
        verification.validateSnapshot();
        return verification;
    }

    public void routeTo(ProviderType provider, Long routingPolicyVersion, LocalDateTime routedAt) {
        VerificationStatus nextStatus = VerificationStateMachine.transit(status, VerificationEvent.ROUTE_SELECTED);
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.routingPolicyVersion = Objects.requireNonNull(routingPolicyVersion, "routingPolicyVersion must not be null");
        this.routedAt = Objects.requireNonNull(routedAt, "routedAt must not be null");
        this.status = nextStatus;
    }

    public void startProviderCall(String providerTransactionId, String providerRequestNo, LocalDateTime providerCalledAt) {
        VerificationStatus nextStatus = VerificationStateMachine.transit(status, VerificationEvent.PROVIDER_CALL_STARTED);
        this.providerTransactionId = requireText(providerTransactionId, "providerTransactionId");
        this.providerRequestNo = requireText(providerRequestNo, "providerRequestNo");
        this.providerCalledAt = Objects.requireNonNull(providerCalledAt, "providerCalledAt must not be null");
        this.status = nextStatus;
    }

    public void recordProviderReturn(String webTransactionId) {
        if (status != VerificationStatus.IN_PROGRESS) {
            throw new IllegalStateException("verification status must be " + VerificationStatus.IN_PROGRESS + " but was " + status);
        }
        this.webTransactionId = requireText(webTransactionId, "webTransactionId");
    }

    public void succeed(LocalDateTime completedAt) {
        complete(VerificationEvent.PROVIDER_CALL_SUCCEEDED, completedAt);
    }

    public void fail(LocalDateTime completedAt) {
        complete(VerificationEvent.PROVIDER_CALL_FAILED, completedAt);
    }

    public void timeout(LocalDateTime completedAt) {
        complete(VerificationEvent.PROVIDER_TIMEOUT, completedAt);
    }

    public void cancel(LocalDateTime completedAt) {
        complete(VerificationEvent.CANCEL_REQUESTED, completedAt);
    }

    public boolean isTerminal() {
        return status.isTerminal();
    }

    public Long getId() {
        return id;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public String getUserId() {
        return userId;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public String getProviderRequestNo() {
        return providerRequestNo;
    }

    public String getWebTransactionId() {
        return webTransactionId;
    }

    public Long getRoutingPolicyVersion() {
        return routingPolicyVersion;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getRoutedAt() {
        return routedAt;
    }

    public LocalDateTime getProviderCalledAt() {
        return providerCalledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getVersion() {
        return version;
    }

    private void complete(VerificationEvent event, LocalDateTime completedAt) {
        VerificationStatus nextStatus = VerificationStateMachine.transit(status, event);
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt must not be null");
        this.status = nextStatus;
    }

    private void validateSnapshot() {
        if (status == VerificationStatus.REQUESTED) {
            requireNull(provider, "provider");
            requireNull(providerTransactionId, "providerTransactionId");
            requireNull(providerRequestNo, "providerRequestNo");
            requireNull(webTransactionId, "webTransactionId");
            requireNull(routingPolicyVersion, "routingPolicyVersion");
            requireNull(routedAt, "routedAt");
            requireNull(providerCalledAt, "providerCalledAt");
            requireNull(completedAt, "completedAt");
            return;
        }

        requireNotNull(provider, "provider");
        requireNotNull(routingPolicyVersion, "routingPolicyVersion");
        requireNotNull(routedAt, "routedAt");

        if (status == VerificationStatus.ROUTED) {
            requireNull(providerTransactionId, "providerTransactionId");
            requireNull(providerRequestNo, "providerRequestNo");
            requireNull(webTransactionId, "webTransactionId");
            requireNull(providerCalledAt, "providerCalledAt");
            requireNull(completedAt, "completedAt");
            return;
        }

        requireText(providerTransactionId, "providerTransactionId");
        requireText(providerRequestNo, "providerRequestNo");
        requireNotNull(providerCalledAt, "providerCalledAt");

        if (status == VerificationStatus.IN_PROGRESS) {
            requireNull(completedAt, "completedAt");
            return;
        }

        requireNotNull(completedAt, "completedAt");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private static void requireNull(Object value, String fieldName) {
        if (value != null) {
            throw new IllegalArgumentException(fieldName + " must be null");
        }
    }
}
