package com.humanizar.nucleorelacionamento.application.dto.acolhimento;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;

public record AcolhimentoCreatedDTO(
        UUID patientId,
        List<NucleoPatientDTO> nucleoPatient) {
}
