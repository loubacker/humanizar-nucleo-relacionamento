package com.humanizar.nucleorelacionamento.application.dto.retrieve;

import java.util.List;
import java.util.UUID;

public record NucleoRelacionamentoRetrieveDTO(
        UUID patientId,
        List<NucleoRelacionamentoDTO> nucleoPatient) {
}
