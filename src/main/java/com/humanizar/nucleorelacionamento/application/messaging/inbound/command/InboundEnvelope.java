package com.humanizar.nucleorelacionamento.application.messaging.inbound.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record InboundEnvelope<T>(
        UUID eventId,
        UUID correlationId,
        String producerService,
        String exchangeName,
        String routingKey,
        String aggregateType,
        UUID aggregateId,
        int eventVersion,
        LocalDateTime occurredAt,
        UUID actorId,
        String userAgent,
        String originIp,
        T payload) {
}
