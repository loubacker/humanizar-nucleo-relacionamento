package com.humanizar.nucleorelacionamento.application.messaging.outbound.event;

import java.util.UUID;

public record ResponsavelPayload(
        UUID responsavelId,
        String role
) {
}
