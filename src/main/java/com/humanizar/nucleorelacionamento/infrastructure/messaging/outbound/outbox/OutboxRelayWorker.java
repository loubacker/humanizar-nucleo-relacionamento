package com.humanizar.nucleorelacionamento.infrastructure.messaging.outbound.outbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.model.OutboxEvent;

@Component
public class OutboxRelayWorker {

    private static final int BATCH_SIZE = 20;

    private final OutboxEventProcessor outboxEventProcessor;

    public OutboxRelayWorker(OutboxEventProcessor outboxEventProcessor) {
        this.outboxEventProcessor = outboxEventProcessor;
    }

    @Scheduled(fixedDelay = 5_000)
    public void relay() {
        List<OutboxEvent> claimed = outboxEventProcessor.claimBatch(BATCH_SIZE);
        for (OutboxEvent event : claimed) {
            outboxEventProcessor.processEvent(event);
        }
    }
}
