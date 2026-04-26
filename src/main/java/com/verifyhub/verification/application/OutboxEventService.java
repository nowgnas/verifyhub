package com.verifyhub.verification.application;

import com.verifyhub.common.time.TimeProvider;
import com.verifyhub.verification.domain.OutboxEvent;
import com.verifyhub.verification.domain.OutboxEventStatus;
import com.verifyhub.verification.port.out.OutboxEventPort;
import org.springframework.stereotype.Service;

@Service
public class OutboxEventService {

    private final OutboxEventPort outboxEventPort;
    private final TimeProvider timeProvider;

    public OutboxEventService(OutboxEventPort outboxEventPort, TimeProvider timeProvider) {
        this.outboxEventPort = outboxEventPort;
        this.timeProvider = timeProvider;
    }

    public OutboxEvent enqueue(String aggregateType, String aggregateId, String eventType, String payload) {
        return outboxEventPort.save(new OutboxEvent(
                null,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.PENDING,
                0,
                timeProvider.now(),
                null
        ));
    }
}
