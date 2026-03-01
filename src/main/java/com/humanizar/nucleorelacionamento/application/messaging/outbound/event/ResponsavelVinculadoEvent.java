package com.humanizar.nucleorelacionamento.application.messaging.outbound.event;

import java.util.List;
import java.util.UUID;

public record ResponsavelVinculadoEvent(
                UUID patientId,
                List<NucleoPatientPayload> nucleoPatient) {
}
