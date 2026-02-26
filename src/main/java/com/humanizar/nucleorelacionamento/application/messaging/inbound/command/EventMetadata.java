package com.humanizar.nucleorelacionamento.application.messaging.inbound.command;

import java.util.UUID;

public record EventMetadata(UUID actorId, String userAgent, String originIp) {

    public static EventMetadata fromEnvelope(InboundEnvelope<?> envelope) {
        if (envelope == null) {
            return new EventMetadata(null, null, null);
        }

        return new EventMetadata(envelope.actorId(), envelope.userAgent(), envelope.originIp());
    }
}
