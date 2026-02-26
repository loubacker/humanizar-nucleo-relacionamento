package com.humanizar.nucleorelacionamento.application.messaging.inbound.command;

import java.util.UUID;

public record ResponsavelCommand(
                UUID responsavelId,
                String role) {
}
