package com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo;

import java.util.List;
import java.util.UUID;

public record NucleoAbordagemCommand(
                UUID nucleoPatientId,
                List<UUID> abordagemId) {
}
