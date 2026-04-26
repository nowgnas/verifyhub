package com.verifyhub.verification.adapter.out.persistence.entity;

import com.verifyhub.verification.domain.ProviderType;
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

@Entity
@Table(name = "late_callback_history")
public class LateCallbackHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", nullable = false, length = 64)
    private String verificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderType provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private VerificationStatus currentStatus;

    @Column(name = "callback_result", nullable = false, length = 30)
    private String callbackResult;

    @Column(nullable = false)
    private Boolean duplicate;

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LateCallbackHistoryEntity() {
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

    public ProviderType getProvider() {
        return provider;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public VerificationStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(VerificationStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCallbackResult() {
        return callbackResult;
    }

    public void setCallbackResult(String callbackResult) {
        this.callbackResult = callbackResult;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
