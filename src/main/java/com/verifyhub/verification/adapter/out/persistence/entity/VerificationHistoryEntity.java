package com.verifyhub.verification.adapter.out.persistence.entity;

import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.domain.VerificationEvent;
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
@Table(name = "verification_history")
public class VerificationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", nullable = false, length = 64)
    private String verificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private VerificationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private VerificationStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private VerificationEvent eventType;

    @Column(length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProviderType provider;

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public VerificationHistoryEntity() {
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

    public VerificationStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(VerificationStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public VerificationStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(VerificationStatus toStatus) {
        this.toStatus = toStatus;
    }

    public VerificationEvent getEventType() {
        return eventType;
    }

    public void setEventType(VerificationEvent eventType) {
        this.eventType = eventType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
