package com.humanizar.nucleorelacionamento.application.messaging.outbound.dto;

import java.util.UUID;

public record OutboundNucleoResponsavelDTO(
                UUID responsavelId,
                String role) {
}
