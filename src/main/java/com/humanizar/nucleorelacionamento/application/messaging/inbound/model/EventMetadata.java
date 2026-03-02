package com.humanizar.nucleorelacionamento.application.messaging.inbound.model;

import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;

public record EventMetadata(UUID actorId, String userAgent, String originIp) {

    public static EventMetadata fromEnvelope(InboundEnvelopeDTO<?> envelope) {
        if (envelope == null) {
            return new EventMetadata(null, null, null);
        }

        return new EventMetadata(envelope.actorId(), envelope.userAgent(), envelope.originIp());
    }
}
