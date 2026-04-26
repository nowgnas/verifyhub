package com.verifyhub.verification.adapter.out.persistence.entity;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationPurpose;
import com.verifyhub.verification.domain.VerificationStatus;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "verification_request")
public class VerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", nullable = false, unique = true, length = 64)
    private String verificationId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationPurpose purpose;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProviderType provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus status;

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Column(name = "provider_request_no", length = 100)
    private String providerRequestNo;

    @Column(name = "web_transaction_id", length = 100)
    private String webTransactionId;

    @Column(name = "routing_policy_version")
    private Long routingPolicyVersion;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "routed_at")
    private LocalDateTime routedAt;

    @Column(name = "provider_called_at")
    private LocalDateTime providerCalledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected VerificationEntity() {
    }

    public VerificationEntity(
            Long id,
            String verificationId,
            String requestId,
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
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.verificationId = verificationId;
        this.requestId = requestId;
        this.purpose = purpose;
        this.idempotencyKey = idempotencyKey;
        this.provider = provider;
        this.status = status;
        this.providerTransactionId = providerTransactionId;
        this.providerRequestNo = providerRequestNo;
        this.webTransactionId = webTransactionId;
        this.routingPolicyVersion = routingPolicyVersion;
        this.requestedAt = requestedAt;
        this.routedAt = routedAt;
        this.providerCalledAt = providerCalledAt;
        this.completedAt = completedAt;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(VerificationPurpose purpose) {
        this.purpose = purpose;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public String getProviderRequestNo() {
        return providerRequestNo;
    }

    public void setProviderRequestNo(String providerRequestNo) {
        this.providerRequestNo = providerRequestNo;
    }

    public String getWebTransactionId() {
        return webTransactionId;
    }

    public void setWebTransactionId(String webTransactionId) {
        this.webTransactionId = webTransactionId;
    }

    public Long getRoutingPolicyVersion() {
        return routingPolicyVersion;
    }

    public void setRoutingPolicyVersion(Long routingPolicyVersion) {
        this.routingPolicyVersion = routingPolicyVersion;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRoutedAt() {
        return routedAt;
    }

    public void setRoutedAt(LocalDateTime routedAt) {
        this.routedAt = routedAt;
    }

    public LocalDateTime getProviderCalledAt() {
        return providerCalledAt;
    }

    public void setProviderCalledAt(LocalDateTime providerCalledAt) {
        this.providerCalledAt = providerCalledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
