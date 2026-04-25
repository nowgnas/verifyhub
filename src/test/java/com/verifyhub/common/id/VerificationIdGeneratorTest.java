package com.verifyhub.common.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerificationIdGeneratorTest {

    @Test
    void generatesVerificationIdWithExpectedPrefix() {
        VerificationIdGenerator generator = new VerificationIdGenerator();

        String verificationId = generator.generate();

        assertThat(verificationId).startsWith("verif_");
        assertThat(verificationId).hasSize(38);
    }
}
