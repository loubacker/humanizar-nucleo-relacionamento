package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa;

import java.util.List;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoAbordagemCommand;

public record ProgramaCreatedCommand(
                List<NucleoAbordagemCommand> nucleoAbordagens) {
}
