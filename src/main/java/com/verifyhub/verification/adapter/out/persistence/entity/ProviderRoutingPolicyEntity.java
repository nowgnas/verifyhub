package com.verifyhub.verification.adapter.out.persistence.entity;

import com.verifyhub.verification.domain.ProviderType;
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
@Table(name = "provider_routing_policy")
public class ProviderRoutingPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderType provider;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProviderRoutingPolicyEntity() {
    }

    public ProviderRoutingPolicyEntity(
            Long id,
            ProviderType provider,
            Integer weight,
            Boolean enabled,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.provider = provider;
        this.weight = weight;
        this.enabled = enabled;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public Integer getWeight() {
        return weight;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
