package com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher;

import com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper.OutboxEventMapper;
import com.humanizar.nucleorelacionamento.domain.model.OutboxEvent;
import com.humanizar.nucleorelacionamento.domain.port.OutboxEventPort;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventPublisher {

    private final OutboxEventPort outboxEventPort;
    private final OutboxEventMapper outboxEventMapper;

    public OutboxEventPublisher(OutboxEventPort outboxEventPort, OutboxEventMapper outboxEventMapper) {
        this.outboxEventPort = outboxEventPort;
        this.outboxEventMapper = outboxEventMapper;
    }

    public void publish(String routingKey, UUID aggregateId,
                        UUID correlationId, Object payload,
                        UUID actorId, String userAgent, String originIp) {
        OutboxEvent event = outboxEventMapper.toOutboxEvent(
                routingKey, aggregateId, correlationId, payload,
                actorId, userAgent, originIp);
        outboxEventPort.save(event);
    }

    public void publish(String routingKey,
                        String aggregateType,
                        UUID aggregateId,
                        UUID eventId,
                        UUID correlationId,
                        Object payload,
                        UUID actorId,
                        String userAgent,
                        String originIp) {
        OutboxEvent event = outboxEventMapper.toOutboxEvent(
                routingKey,
                aggregateType,
                aggregateId,
                eventId,
                correlationId,
                payload,
                actorId,
                userAgent,
                originIp);
        outboxEventPort.save(event);
    }
}
