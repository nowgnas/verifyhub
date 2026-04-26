package com.verifyhub.common.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequestIdGeneratorTest {

    @Test
    void generatesRequestIdWithExpectedPrefix() {
        RequestIdGenerator generator = new RequestIdGenerator();

        String requestId = generator.generate();

        assertThat(requestId).startsWith("req_");
        assertThat(requestId).hasSize(36);
    }
}
