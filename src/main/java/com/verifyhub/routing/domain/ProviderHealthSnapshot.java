package com.verifyhub.routing.domain;

import com.verifyhub.verification.domain.ProviderType;
import java.util.Objects;

public record ProviderHealthSnapshot(
        ProviderType provider,
        boolean available,
        String reason
) {

    public ProviderHealthSnapshot {
        Objects.requireNonNull(provider, "provider must not be null");
    }

    public static ProviderHealthSnapshot available(ProviderType provider) {
        return new ProviderHealthSnapshot(provider, true, null);
    }

    public static ProviderHealthSnapshot unavailable(ProviderType provider, String reason) {
        return new ProviderHealthSnapshot(provider, false, reason);
    }
}
