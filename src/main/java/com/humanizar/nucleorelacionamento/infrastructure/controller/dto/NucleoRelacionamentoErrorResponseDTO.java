package com.humanizar.nucleorelacionamento.infrastructure.controller.dto;

import java.time.OffsetDateTime;

public record NucleoRelacionamentoErrorResponseDTO(
        int status,
        String reasonCode,
        String message,
        String path,
        OffsetDateTime timestamp) {
}
