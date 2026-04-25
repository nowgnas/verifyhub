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
                requestedAt,
                null,
                null,
                null,
                0L
        );
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

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
