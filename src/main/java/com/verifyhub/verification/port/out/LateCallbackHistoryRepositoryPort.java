package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.LateCallbackHistory;

public interface LateCallbackHistoryRepositoryPort {

    LateCallbackHistory save(LateCallbackHistory lateCallbackHistory);
}
