package com.verifyhub.common.id;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VerificationIdGenerator {

    private static final String PREFIX = "verif_";

    public String generate() {
        return PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}
