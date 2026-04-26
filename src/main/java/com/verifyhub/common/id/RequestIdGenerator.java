package com.verifyhub.common.id;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RequestIdGenerator {

    private static final String PREFIX = "req_";

    public String generate() {
        return PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}
