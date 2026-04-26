package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.ProviderCallHistory;

public interface ProviderCallHistoryRepositoryPort {

    ProviderCallHistory save(ProviderCallHistory providerCallHistory);
}
