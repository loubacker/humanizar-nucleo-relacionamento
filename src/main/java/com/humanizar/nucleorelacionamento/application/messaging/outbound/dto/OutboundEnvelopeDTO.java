package com.humanizar.nucleorelacionamento.application.messaging.outbound.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboundEnvelopeDTO<T>(
        UUID eventId,
        UUID correlationId,
        String producerService,
        LocalDateTime occurredAt,
        UUID actorId,
        String userAgent,
        String originIp,
        T payload) {
}
