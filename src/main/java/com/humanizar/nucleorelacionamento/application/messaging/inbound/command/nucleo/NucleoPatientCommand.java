package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.ResponsavelCommand;

public record NucleoPatientCommand(
        UUID nucleoId,
        List<ResponsavelCommand> nucleoPatientResponsavel) {
}
