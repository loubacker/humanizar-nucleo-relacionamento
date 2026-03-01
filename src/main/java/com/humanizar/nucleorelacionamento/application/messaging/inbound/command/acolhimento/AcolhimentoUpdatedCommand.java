package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoPatientCommand;

public record AcolhimentoUpdatedCommand(
        UUID patientId,
        List<NucleoPatientCommand> nucleoPatient) {
}
