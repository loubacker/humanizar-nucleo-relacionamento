package com.humanizar.nucleorelacionamento.application.dto;

import java.util.UUID;

public record BlockedNucleoDTO(
        UUID nucleoId,
        Integer abordagemCount) {
}
