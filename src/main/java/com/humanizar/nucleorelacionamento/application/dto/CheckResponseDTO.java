package com.humanizar.nucleorelacionamento.application.dto;

import java.util.List;

public record CheckResponseDTO(
        Boolean canDelete,
        String reasonCode,
        String message,
        List<BlockedNucleoDTO> blockedNucleos) {
}
