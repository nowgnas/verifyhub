package com.verifyhub.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerifyhubExceptionTest {

    @Test
    void exposesErrorCodeAndMessage() {
        VerifyhubException exception = new VerifyhubException(ErrorCode.PROVIDER_UNAVAILABLE);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_UNAVAILABLE);
        assertThat(exception.getMessage()).isEqualTo("No available verification provider");
    }
}
