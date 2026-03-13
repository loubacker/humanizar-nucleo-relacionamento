package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.CallbackDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class OutboundCallbackMapperTest {

    private final OutboundCallbackMapper mapper = new OutboundCallbackMapper();

    @Test
    void shouldMapProcessedCallbackWithTraceMetadata() {
        InboundEnvelopeDTO<Object> inbound = inboundEnvelope(UUID.randomUUID(), UUID.randomUUID());

        CallbackDTO callback = mapper.toProcessedCallback(
                inbound,
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1);

        assertEquals(inbound.eventId(), callback.eventId());
        assertEquals(inbound.correlationId(), callback.correlationId());
        assertEquals("humanizar-nucleo-relacionamento", callback.producerService());
        assertEquals(RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1, callback.routingKey());
        assertEquals("PROCESSED", callback.status());
        assertNotNull(callback.occurredAt());
        assertNotNull(callback.processedAt());
    }

    @Test
    void shouldFailFastWhenInboundEventIdIsMissing() {
        InboundEnvelopeDTO<Object> inbound = inboundEnvelope(null, UUID.randomUUID());

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toProcessedCallback(
                        inbound,
                        RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                        ExchangeCatalog.ACOLHIMENTO_EVENT,
                        RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    private InboundEnvelopeDTO<Object> inboundEnvelope(UUID eventId, UUID correlationId) {
        return new InboundEnvelopeDTO<>(
                eventId,
                correlationId,
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                new Object());
    }
}
