package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundNucleoResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelDesvinculadoDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class OutboundResponsavelDesvinculadoMapperTest {

    private final OutboundResponsavelDesvinculadoMapper mapper = new OutboundResponsavelDesvinculadoMapper();

    @Test
    void shouldBuildEnvelopeWithPayload() {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        OutboundResponsavelDesvinculadoDTO payload = payload();

        OutboundEnvelopeDTO<OutboundResponsavelDesvinculadoDTO> envelope = mapper.toEnvelope(
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
    void shouldFailFastWhenPayloadIsMissing() {
        UUID correlationId = UUID.randomUUID();

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toEnvelope(
                        UUID.randomUUID(),
                        correlationId,
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1",
                        null));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    private OutboundResponsavelDesvinculadoDTO payload() {
        OutboundNucleoResponsavelDTO responsavel = new OutboundNucleoResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR");
        OutboundNucleoPatientDTO nucleo = new OutboundNucleoPatientDTO(UUID.randomUUID(), List.of(responsavel));
        return new OutboundResponsavelDesvinculadoDTO(UUID.randomUUID(), List.of(nucleo));
    }
}
