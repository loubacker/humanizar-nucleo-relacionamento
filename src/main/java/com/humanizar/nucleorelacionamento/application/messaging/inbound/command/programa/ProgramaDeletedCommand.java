package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa;

import java.util.UUID;

public record ProgramaDeletedCommand(
        UUID patientId
) {
}
