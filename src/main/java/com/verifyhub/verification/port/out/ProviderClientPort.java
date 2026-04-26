package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderType;

public interface ProviderClientPort {

    ProviderType providerType();

    ProviderRequestResult requestVerification(ProviderRequest request);

    ProviderResult requestResult(ProviderResultRequest request);
}
