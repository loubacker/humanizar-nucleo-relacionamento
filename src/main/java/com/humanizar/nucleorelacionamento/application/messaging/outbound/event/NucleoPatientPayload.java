package com.humanizar.nucleorelacionamento.application.messaging.outbound.event;

import java.util.List;
import java.util.UUID;

public record NucleoPatientPayload(
                UUID nucleoId,
                List<ResponsavelPayload> nucleoPatientResponsavel) {
}
