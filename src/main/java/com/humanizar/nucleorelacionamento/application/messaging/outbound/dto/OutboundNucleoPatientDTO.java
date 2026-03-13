package com.humanizar.nucleorelacionamento.application.messaging.outbound.dto;

import java.util.List;
import java.util.UUID;

public record OutboundNucleoPatientDTO(
        UUID nucleoPatientId,
        List<OutboundNucleoResponsavelDTO> nucleoPatientResponsavel) {
}
