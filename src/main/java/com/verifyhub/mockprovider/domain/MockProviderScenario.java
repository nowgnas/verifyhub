package com.verifyhub.mockprovider.domain;

public enum MockProviderScenario {
    SUCCESS,
    FAIL,
    TIMEOUT,
    HTTP_500,
    DELAYED_RETURN,
    DUPLICATE_RETURN,
    INVALID_INTEGRITY_RESULT
}
