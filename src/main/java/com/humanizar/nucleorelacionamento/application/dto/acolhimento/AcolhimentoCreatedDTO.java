package com.humanizar.nucleorelacionamento.application.dto.acolhimento;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;

public record AcolhimentoCreatedDTO(
        UUID patientId,
        UUID nucleoId,
        List<ResponsavelDTO> nucleoPatientResponsavel) {
}
