package com.humanizar.nucleorelacionamento.application.dto;

import java.util.List;
import java.util.UUID;

public record NucleoPatientDTO(
        UUID nucleoId,
        List<ResponsavelDTO> nucleoPatientResponsavel) {
}
