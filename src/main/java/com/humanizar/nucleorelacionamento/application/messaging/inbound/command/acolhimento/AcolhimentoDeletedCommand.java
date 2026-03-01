package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento;

import java.util.UUID;

public record AcolhimentoDeletedCommand(
                UUID patientId) {
}
