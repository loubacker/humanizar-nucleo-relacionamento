package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.OutboxEvent;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class OutboxEventMapperTest {

    private final OutboxEventMapper mapper = new OutboxEventMapper(new ObjectMapper().findAndRegisterModules());

    @Test
    void shouldUseProvidedExchangeWhenExplicitlyInformed() {
        UUID aggregateId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        OutboxEvent event = mapper.toOutboxEvent(
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1,
                "acolhimento",
                aggregateId,
                eventId,
                correlationId,
                Map.of("status", "PROCESSED"),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        assertEquals(ExchangeCatalog.ACOLHIMENTO_EVENT, event.getExchangeName());
        assertEquals(RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1, event.getRoutingKey());
        assertEquals(eventId, event.getEventId());
        assertEquals(correlationId, event.getCorrelationId());
        assertNotNull(event.getPayload());
    }

    @Test
    void shouldFailFastWhenCorrelationIdIsMissing() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toOutboxEvent(
                        ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT,
                        RoutingKeyCatalog.RESPONSAVEL_VINCULADO_V1,
                        "nucleo_patient",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        Map.of("status", "REJECTED"),
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1"));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void shouldSerializeEnvelopeWithMetadataAndPayload() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboundEnvelopeDTO<Map<String, Object>> envelope = new OutboundEnvelopeDTO<>(
                eventId,
                correlationId,
                "humanizar-nucleo-relacionamento",
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                Map.of("patientId", UUID.randomUUID().toString()));

        OutboxEvent outboxEvent = mapper.toOutboxEvent(
                ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT,
                RoutingKeyCatalog.RESPONSAVEL_VINCULADO_V1,
                "nucleo_patient",
                aggregateId,
                eventId,
                correlationId,
                envelope,
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        @SuppressWarnings("unchecked")
        Map<String, Object> serialized = new ObjectMapper().readValue(outboxEvent.getPayload(), Map.class);
        assertEquals(eventId.toString(), serialized.get("eventId"));
        assertEquals(correlationId.toString(), serialized.get("correlationId"));
        assertNotNull(serialized.get("payload"));
    }
}
