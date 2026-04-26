package com.verifyhub.verification.port.out;

import com.verifyhub.verification.domain.OutboxEvent;

public interface OutboxEventPort {

    OutboxEvent save(OutboxEvent outboxEvent);
}
