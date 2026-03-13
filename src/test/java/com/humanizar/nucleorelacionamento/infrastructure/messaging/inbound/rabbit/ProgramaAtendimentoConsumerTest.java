package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa.ProgramaCreatedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa.ProgramaDeletedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa.ProgramaRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa.ProgramaUpdatedRoutingHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.EnvelopeInboundMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaCreateMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaDeleteMapper;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaUpdateMapper;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaUpdatedUseCase;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.config.rabbit.RabbitAcknowledgementConfig;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class ProgramaAtendimentoConsumerTest {

    @Mock
    private EnvelopeInboundMapper envelopeInboundMapper;

    @Mock
    private InboundProgramaCreateMapper inboundProgramaCreateMapper;

    @Mock
    private InboundProgramaUpdateMapper inboundProgramaUpdateMapper;

    @Mock
    private InboundProgramaDeleteMapper inboundProgramaDeleteMapper;

    @Mock
    private ProcessedEventGuard processedEventGuard;

    @Mock
    private ProcessingResultPublisher processingResultPublisher;

    @Mock
    private ProgramaCreatedUseCase programaCreatedUseCase;

    @Mock
    private ProgramaUpdatedUseCase programaUpdatedUseCase;

    @Mock
    private ProgramaDeletedUseCase programaDeletedUseCase;

    @Mock
    private Channel channel;

    @Test
    void shouldAckAndPublishProcessedWhenCreatedSucceeds() throws IOException {
        long deliveryTag = 20L;

        InboundEnvelopeDTO<Object> envelope = baseEnvelope(RoutingKeyCatalog.PROGRAMA_CREATED_V1, new Object());
        List<ProgramaDTO> payload = List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID())));

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class))).thenReturn(envelope);
        when(inboundProgramaCreateMapper.toPayload(envelope)).thenReturn(payload);
        when(programaCreatedUseCase.execute(any(), any(), any(), any())).thenReturn(EventOutcome.success());

        ProgramaAtendimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag), channel);

        verify(programaCreatedUseCase).execute(any(), eq(RoutingKeyCatalog.PROGRAMA_CREATED_V1), eq(envelope),
                eq(payload));
        verify(channel).basicAck(deliveryTag, false);
        verify(processingResultPublisher).publishProcessed(envelope, RoutingKeyCatalog.PROGRAMA_CREATED_V1);
        verify(programaUpdatedUseCase, never()).execute(any(), any(), any(), any());
        verify(programaDeletedUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void shouldAckAndPublishRejectedWhenUseCaseReturnsFunctionalFailure() throws IOException {
        long deliveryTag = 21L;

        InboundEnvelopeDTO<Object> envelope = baseEnvelope(RoutingKeyCatalog.PROGRAMA_CREATED_V1, new Object());
        List<ProgramaDTO> payload = List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID())));

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class))).thenReturn(envelope);
        when(inboundProgramaCreateMapper.toPayload(envelope)).thenReturn(payload);
        when(programaCreatedUseCase.execute(any(), any(), any(), any()))
                .thenReturn(EventOutcome.failed(ReasonCode.VALIDATION_ERROR));

        ProgramaAtendimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag), channel);

        verify(channel).basicAck(deliveryTag, false);
        verify(processingResultPublisher).publishRejected(eq(envelope), eq(RoutingKeyCatalog.PROGRAMA_CREATED_V1),
                any());
    }

    @Test
    void shouldNackDeadLetterWhenInboundParseFails() throws IOException {
        long deliveryTag = 22L;

        when(envelopeInboundMapper.parseEnvelope(any(byte[].class)))
                .thenThrow(new NucleoRelacionamentoException(ReasonCode.INBOUND_PARSE_ERROR, null));

        ProgramaAtendimentoConsumer consumer = buildConsumer();
        consumer.onMessage(message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag), channel);

        verify(channel).basicNack(deliveryTag, false, false);
        verify(programaCreatedUseCase, never()).execute(any(), any(), any(), any());
    }

    private ProgramaAtendimentoConsumer buildConsumer() {
        ProgramaRoutingHandler createdHandler = new ProgramaCreatedRoutingHandler(
                inboundProgramaCreateMapper,
                programaCreatedUseCase);
        ProgramaRoutingHandler updatedHandler = new ProgramaUpdatedRoutingHandler(
                inboundProgramaUpdateMapper,
                programaUpdatedUseCase);
        ProgramaRoutingHandler deletedHandler = new ProgramaDeletedRoutingHandler(
                inboundProgramaDeleteMapper,
                programaDeletedUseCase);

        return new ProgramaAtendimentoConsumer(
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
        properties.setConsumerQueue("q.programa");
        return new Message("{}".getBytes(), properties);
    }

    private InboundEnvelopeDTO<Object> baseEnvelope(String routingKey, Object payload) {
        return new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-programa",
                "humanizar.programa.command",
                routingKey,
                "programa",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }
}
