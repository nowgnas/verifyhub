package com.verifyhub.verification.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record OutboxEvent(
        Long id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        OutboxEventStatus status,
        int retryCount,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {

    public OutboxEvent {
        requireText(aggregateType, "aggregateType");
        requireText(aggregateId, "aggregateId");
        requireText(eventType, "eventType");
        requireText(payload, "payload");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
