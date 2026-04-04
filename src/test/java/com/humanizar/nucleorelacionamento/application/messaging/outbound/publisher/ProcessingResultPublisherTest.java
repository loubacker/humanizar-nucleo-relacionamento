package com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.CallbackDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper.OutboundCallbackMapper;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@ExtendWith(MockitoExtension.class)
class ProcessingResultPublisherTest {

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    private ProcessingResultPublisher processingResultPublisher;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        processingResultPublisher = new ProcessingResultPublisher(
                outboxEventPublisher,
                new OutboundCallbackMapper());
    }

    @Test
    void shouldPublishProcessedForAcolhimentoRouting() {
        InboundEnvelopeDTO<Object> envelope = envelope(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, "acolhimento");

        processingResultPublisher.publishProcessed(envelope, RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);

        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.ACOLHIMENTO_EVENT),
                eq(RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1),
                eq("acolhimento"),
                eq(envelope.aggregateId()),
                eq(envelope.eventId()),
                eq(envelope.correlationId()),
                payloadCaptor.capture(),
                eq(envelope.actorId()),
                eq(envelope.userAgent()),
                eq(envelope.originIp()));

        assertNotNull(payloadCaptor.getValue());
        CallbackDTO payload = (CallbackDTO) payloadCaptor.getValue();
        assertEquals(ExchangeCatalog.ACOLHIMENTO_EVENT, payload.exchangeName());
        assertEquals(RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1, payload.routingKey());
    }

    @Test
    void shouldPublishRejectedForProgramaRouting() {
        InboundEnvelopeDTO<Object> envelope = envelope(RoutingKeyCatalog.PROGRAMA_UPDATED_V1, "programa-atendimento");
        EventOutcome outcome = EventOutcome.failed(ReasonCode.VALIDATION_ERROR);

        processingResultPublisher.publishRejected(envelope, RoutingKeyCatalog.PROGRAMA_UPDATED_V1, outcome);

        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.PROGRAMA_EVENT),
                eq(RoutingKeyCatalog.PROGRAMA_REJECTED_V1),
                eq("programa-atendimento"),
                eq(envelope.aggregateId()),
                eq(envelope.eventId()),
                eq(envelope.correlationId()),
                payloadCaptor.capture(),
                eq(envelope.actorId()),
                eq(envelope.userAgent()),
                eq(envelope.originIp()));

        assertNotNull(payloadCaptor.getValue());
        CallbackDTO payload = (CallbackDTO) payloadCaptor.getValue();
        assertEquals(ExchangeCatalog.PROGRAMA_EVENT, payload.exchangeName());
        assertEquals(RoutingKeyCatalog.PROGRAMA_REJECTED_V1, payload.routingKey());
        assertEquals(false, outcome.retryable());
    }

    private InboundEnvelopeDTO<Object> envelope(String routingKey, String aggregateType) {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        return new InboundEnvelopeDTO<>(
                eventId,
                correlationId,
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                routingKey,
                aggregateType,
                aggregateId,
                1,
                LocalDateTime.now(),
                actorId,
                "JUnit",
                "127.0.0.1",
                new Object());
    }
}
