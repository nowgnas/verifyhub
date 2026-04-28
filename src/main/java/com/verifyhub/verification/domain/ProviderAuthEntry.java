package com.verifyhub.verification.domain;

import java.util.Map;
import java.util.Objects;

public record ProviderAuthEntry(
        ProviderType provider,
        AuthEntryType type,
        String url,
        String method,
        String charset,
        Map<String, String> fields
) {

    public ProviderAuthEntry {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(type, "type must not be null");
        requireText(url, "url");
        requireText(method, "method");
        requireText(charset, "charset");
        fields = Map.copyOf(Objects.requireNonNull(fields, "fields must not be null"));
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
