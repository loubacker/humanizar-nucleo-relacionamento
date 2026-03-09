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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.mapper.ProgramaInboundMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.EnvelopeValidator;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaUpdatedUseCase;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@ExtendWith(MockitoExtension.class)
class ProgramaAtendimentoConsumerTest {

        @Mock
        private ObjectMapper objectMapper;

        @Mock
        private EnvelopeValidator envelopeValidator;

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
        @SuppressWarnings("unchecked")
        void shouldRouteCreatedToCreatedUseCaseAndAck() throws IOException {
                long deliveryTag = 11L;
                InboundEnvelopeDTO<Object> envelopeDto = baseEnvelope(RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                new Object());
                InboundEnvelopeDTO<List<ProgramaDTO>> createdEnvelopeDto = baseEnvelope(
                                RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID()))));

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, createdEnvelopeDto);
                when(programaCreatedUseCase.execute(any(), any(), any(), any()))
                                .thenReturn(EventOutcome.success());

                ProgramaAtendimentoConsumer consumer = buildConsumer();
                Message message = message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag);

                consumer.onMessage(message, channel);

                verify(programaCreatedUseCase).execute(any(), eq(RoutingKeyCatalog.PROGRAMA_CREATED_V1), any(), any());
                verify(programaUpdatedUseCase, never()).execute(any(), any(), any(), any());
                verify(programaDeletedUseCase, never()).execute(any(), any(), any(), any());
                verify(channel).basicAck(deliveryTag, false);
                verify(processingResultPublisher).publishProcessed(any(), eq(RoutingKeyCatalog.PROGRAMA_CREATED_V1));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldRouteUpdatedToUpdatedUseCaseAndAck() throws IOException {
                long deliveryTag = 12L;
                InboundEnvelopeDTO<Object> envelopeDto = baseEnvelope(RoutingKeyCatalog.PROGRAMA_UPDATED_V1,
                                new Object());
                InboundEnvelopeDTO<List<ProgramaDTO>> updatedEnvelopeDto = baseEnvelope(
                                RoutingKeyCatalog.PROGRAMA_UPDATED_V1,
                                List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID()))));

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, updatedEnvelopeDto);
                when(programaUpdatedUseCase.execute(any(), any(), any(), any()))
                                .thenReturn(EventOutcome.success());

                ProgramaAtendimentoConsumer consumer = buildConsumer();
                Message message = message(RoutingKeyCatalog.PROGRAMA_UPDATED_V1, deliveryTag);

                consumer.onMessage(message, channel);

                verify(programaUpdatedUseCase).execute(any(), eq(RoutingKeyCatalog.PROGRAMA_UPDATED_V1), any(), any());
                verify(channel).basicAck(deliveryTag, false);
                verify(processingResultPublisher).publishProcessed(any(), eq(RoutingKeyCatalog.PROGRAMA_UPDATED_V1));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldRouteDeletedToDeletedUseCaseAndAck() throws IOException {
                long deliveryTag = 13L;
                InboundEnvelopeDTO<Object> envelopeDto = baseEnvelope(RoutingKeyCatalog.PROGRAMA_DELETED_V1,
                                new Object());
                InboundEnvelopeDTO<ProgramaDeletedDTO> deletedEnvelopeDto = baseEnvelope(
                                RoutingKeyCatalog.PROGRAMA_DELETED_V1,
                                new ProgramaDeletedDTO(UUID.randomUUID()));

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, deletedEnvelopeDto);
                when(programaDeletedUseCase.execute(any(), any(), any(), any()))
                                .thenReturn(EventOutcome.success());

                ProgramaAtendimentoConsumer consumer = buildConsumer();
                Message message = message(RoutingKeyCatalog.PROGRAMA_DELETED_V1, deliveryTag);

                consumer.onMessage(message, channel);

                verify(programaDeletedUseCase).execute(any(), eq(RoutingKeyCatalog.PROGRAMA_DELETED_V1), any(), any());
                verify(channel).basicAck(deliveryTag, false);
                verify(processingResultPublisher).publishProcessed(any(), eq(RoutingKeyCatalog.PROGRAMA_DELETED_V1));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldNackWithRequeueWhenRetryableError() throws IOException {
                long deliveryTag = 14L;
                InboundEnvelopeDTO<Object> envelopeDto = baseEnvelope(RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                new Object());
                InboundEnvelopeDTO<List<ProgramaDTO>> createdEnvelopeDto = baseEnvelope(
                                RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID()))));

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, createdEnvelopeDto);
                when(programaCreatedUseCase.execute(any(), any(), any(), any()))
                                .thenThrow(new NucleoRelacionamentoException(ReasonCode.PERSISTENCE_FAILURE,
                                                UUID.randomUUID().toString()));

                ProgramaAtendimentoConsumer consumer = buildConsumer();
                Message message = message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag);

                consumer.onMessage(message, channel);

                verify(channel).basicNack(deliveryTag, false, true);
                verify(processingResultPublisher, never()).publishRejected(any(), any(), any());
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldAckAndPublishRejectedWhenNonRetryableError() throws IOException {
                long deliveryTag = 15L;
                InboundEnvelopeDTO<Object> envelopeDto = baseEnvelope(RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                new Object());
                InboundEnvelopeDTO<List<ProgramaDTO>> createdEnvelopeDto = baseEnvelope(
                                RoutingKeyCatalog.PROGRAMA_CREATED_V1,
                                List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID()))));

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, createdEnvelopeDto);
                when(programaCreatedUseCase.execute(any(), any(), any(), any()))
                                .thenThrow(new NucleoRelacionamentoException(ReasonCode.VALIDATION_ERROR,
                                                UUID.randomUUID().toString()));

                ProgramaAtendimentoConsumer consumer = buildConsumer();
                Message message = message(RoutingKeyCatalog.PROGRAMA_CREATED_V1, deliveryTag);

                consumer.onMessage(message, channel);

                verify(channel).basicAck(deliveryTag, false);
                verify(processingResultPublisher).publishRejected(any(), eq(RoutingKeyCatalog.PROGRAMA_CREATED_V1),
                                any());
        }

        private ProgramaAtendimentoConsumer buildConsumer() {
                return new ProgramaAtendimentoConsumer(
                                objectMapper,
                                envelopeValidator,
                                processedEventGuard,
                                processingResultPublisher,
                                new InboundEnvelopeMapper(),
                                new ProgramaInboundMapper(),
                                programaCreatedUseCase,
                                programaUpdatedUseCase,
                                programaDeletedUseCase);
        }

        private Message message(String routingKey, long deliveryTag) {
                MessageProperties properties = new MessageProperties();
                properties.setReceivedRoutingKey(routingKey);
                properties.setDeliveryTag(deliveryTag);
                return new Message("{}".getBytes(), properties);
        }

        private <T> InboundEnvelopeDTO<T> baseEnvelope(String routingKey, T payload) {
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
