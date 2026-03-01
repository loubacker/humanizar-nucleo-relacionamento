package com.humanizar.nucleorelacionamento.application.messaging.outbound.event;

import java.util.List;
import java.util.UUID;

public record ResponsavelDesvinculadoEvent(
                UUID patientId,
                List<NucleoPatientPayload> nucleoPatient) {
}
