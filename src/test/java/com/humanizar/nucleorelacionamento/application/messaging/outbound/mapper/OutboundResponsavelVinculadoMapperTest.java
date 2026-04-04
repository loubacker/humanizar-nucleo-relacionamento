package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelVinculadoDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class OutboundResponsavelVinculadoMapperTest {

    private final OutboundResponsavelVinculadoMapper mapper = new OutboundResponsavelVinculadoMapper();

    @Test
    void shouldBuildEnvelopeWithPayload() {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        OutboundResponsavelVinculadoDTO payload = payload();

        OutboundEnvelopeDTO<OutboundResponsavelVinculadoDTO> envelope = mapper.toEnvelope(
                eventId,
                correlationId,
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);

        assertEquals(eventId, envelope.eventId());
        assertEquals(correlationId, envelope.correlationId());
        assertEquals("humanizar-nucleo-relacionamento", envelope.producerService());
        assertEquals(payload, envelope.payload());
        assertNotNull(envelope.occurredAt());
    }

    @Test
    void shouldFailFastWhenCorrelationIdIsMissing() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toEnvelope(
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1",
                        payload()));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    private OutboundResponsavelVinculadoDTO payload() {
        OutboundNucleoResponsavelDTO responsavel = new OutboundNucleoResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR");
        OutboundNucleoPatientDTO nucleo = new OutboundNucleoPatientDTO(UUID.randomUUID(), List.of(responsavel));
        return new OutboundResponsavelVinculadoDTO(UUID.randomUUID(), List.of(nucleo));
    }
}
