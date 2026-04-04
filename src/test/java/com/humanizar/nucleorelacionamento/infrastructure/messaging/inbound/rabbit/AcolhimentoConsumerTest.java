package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento.AcolhimentoCreatedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento.AcolhimentoDeletedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento.AcolhimentoRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento.AcolhimentoUpdatedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.EnvelopeInboundMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoCreateMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoDeleteMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoUpdateMapper;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoUpdatedUseCase;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.config.rabbit.RabbitAcknowledgementConfig;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class AcolhimentoConsumerTest {

    @Mock
    private EnvelopeInboundMapper envelopeInboundMapper;

    @Mock
    private InboundAcolhimentoCreateMapper inboundAcolhimentoCreateMapper;

    @Mock
    private InboundAcolhimentoUpdateMapper inboundAcolhimentoUpdateMapper;

    @Mock
    private InboundAcolhimentoDeleteMapper inboundAcolhimentoDeleteMapper;

    @Mock
    private ProcessedEventGuard processedEventGuard;

    @Mock
    private ProcessingResultPublisher processingResultPublisher;

    @Mock
    private AcolhimentoCreatedUseCase acolhimentoCreatedUseCase;

    @Mock
    private AcolhimentoUpdatedUseCase acolhimentoUpdatedUseCase;

    @Mock
    private AcolhimentoDeletedUseCase acolhimentoDeletedUseCase;

    @Mock
    private Channel channel;

    @Test
    void shouldAckAndPublishProcessedWhenCreatedSucceeds() throws IOException {
        long deliveryTag = 10L;

        InboundEnvelopeDTO<Object> envelope = baseEnvelope(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, new Object());
        AcolhimentoCreatedDTO payload = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(new NucleoPatientDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class))).thenReturn(envelope);
        when(inboundAcolhimentoCreateMapper.toPayload(envelope)).thenReturn(payload);
        when(acolhimentoCreatedUseCase.execute(any(), any(), any(), any())).thenReturn(EventOutcome.success());

        AcolhimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, deliveryTag), channel);

        verify(acolhimentoCreatedUseCase).execute(any(), eq(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1), eq(envelope),
                eq(payload));
        verify(channel).basicAck(deliveryTag, false);
        verify(processingResultPublisher).publishProcessed(envelope, RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
        verify(acolhimentoUpdatedUseCase, never()).execute(any(), any(), any(), any());
        verify(acolhimentoDeletedUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void shouldNackRetryWhenUseCaseReturnsRetryableOutcome() throws IOException {
        long deliveryTag = 11L;

        InboundEnvelopeDTO<Object> envelope = baseEnvelope(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, new Object());
        AcolhimentoCreatedDTO payload = new AcolhimentoCreatedDTO(UUID.randomUUID(), List.of());

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class))).thenReturn(envelope);
        when(inboundAcolhimentoCreateMapper.toPayload(envelope)).thenReturn(payload);
        when(acolhimentoCreatedUseCase.execute(any(), any(), any(), any()))
                .thenReturn(EventOutcome.failed(ReasonCode.PERSISTENCE_FAILURE));

        AcolhimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, deliveryTag), channel);

        verify(channel).basicNack(deliveryTag, false, true);
        verify(processingResultPublisher, never()).publishRejected(any(), any(), any());
    }

    @Test
    void shouldNackDeadLetterWhenInboundParseFails() throws IOException {
        long deliveryTag = 12L;

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class)))
                .thenThrow(new NucleoRelacionamentoException(ReasonCode.INBOUND_PARSE_ERROR, null));

        AcolhimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1, deliveryTag), channel);

        verify(channel).basicNack(deliveryTag, false, false);
        verify(acolhimentoCreatedUseCase, never()).execute(any(), any(), any(), any());
    }

    private AcolhimentoConsumer buildConsumer() {
        AcolhimentoRoutingHandler createdHandler = new AcolhimentoCreatedRoutingHandler(
                inboundAcolhimentoCreateMapper,
                acolhimentoCreatedUseCase);
        AcolhimentoRoutingHandler updatedHandler = new AcolhimentoUpdatedRoutingHandler(
                inboundAcolhimentoUpdateMapper,
                acolhimentoUpdatedUseCase);
        AcolhimentoRoutingHandler deletedHandler = new AcolhimentoDeletedRoutingHandler(
                inboundAcolhimentoDeleteMapper,
                acolhimentoDeletedUseCase);

        return new AcolhimentoConsumer(
                envelopeInboundMapper,
                processedEventGuard,
                processingResultPublisher,
                List.of(createdHandler, updatedHandler, deletedHandler),
                new RabbitAcknowledgementConfig());
    }

    private Message message(String routingKey, long deliveryTag) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedRoutingKey(routingKey);
        properties.setDeliveryTag(deliveryTag);
        properties.setMessageId(UUID.randomUUID().toString());
        properties.setConsumerQueue("q.acolhimento");
        return new Message("{}".getBytes(), properties);
    }

    private InboundEnvelopeDTO<Object> baseEnvelope(String routingKey, Object payload) {
        return new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                routingKey,
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }
}
