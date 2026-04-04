package com.humanizar.nucleorelacionamento.application.dto.retrieve;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;

public record NucleoRelacionamentoDTO(
        UUID nucleoPatientId,
        UUID nucleoId,
        List<ResponsavelDTO> nucleoPatientResponsavel,
        List<UUID> abordagemId) {
}
