package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.ResponsavelCommand;

public record AcolhimentoCreatedCommand(
                UUID patientId,
                UUID nucleoId,
                List<ResponsavelCommand> nucleoPatientResponsavel) {
}
