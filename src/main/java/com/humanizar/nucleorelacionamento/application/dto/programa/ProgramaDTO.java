package com.humanizar.nucleorelacionamento.application.dto.programa;

import java.util.List;
import java.util.UUID;

public record ProgramaDTO(
        UUID nucleoPatientId,
        List<UUID> abordagemId) {
}
