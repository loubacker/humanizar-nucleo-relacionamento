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
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.mapper.AcolhimentoInboundMapper;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.EnvelopeValidator;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoUpdatedUseCase;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@ExtendWith(MockitoExtension.class)
class AcolhimentoConsumerTest {

        @Mock
        private ObjectMapper objectMapper;

        @Mock
        private EnvelopeValidator envelopeValidator;

        @Mock
        private ProcessedEventGuard processedEventGuard;

        @Mock
        private ProcessingResultPublisher processingResultPublisher;

        @Mock
        private AcolhimentoCreatedUseCase processAcolhimentoCreatedUseCase;

        @Mock
        private AcolhimentoUpdatedUseCase processAcolhimentoUpdatedUseCase;

        @Mock
        private AcolhimentoDeletedUseCase processAcolhimentoDeletedUseCase;

        @Mock
        private Channel channel;

        @Test
        @SuppressWarnings("unchecked")
        void shouldRouteCreatedToCreatedUseCaseAndAck() throws IOException {
                UUID eventId = UUID.randomUUID();
                UUID correlationId = UUID.randomUUID();
                UUID aggregateId = UUID.randomUUID();
                UUID actorId = UUID.randomUUID();
                long deliveryTag = 55L;

                InboundEnvelopeDTO<Object> envelopeDto = new InboundEnvelopeDTO<>(
                                eventId,
                                correlationId,
                                "humanizar-acolhimento",
                                "humanizar.acolhimento.event",
                                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                                "acolhimento",
                                aggregateId,
                                1,
                                LocalDateTime.now(),
                                actorId,
                                "JUnit",
                                "127.0.0.1",
                                new Object());

                AcolhimentoCreatedDTO createdPayload = new AcolhimentoCreatedDTO(
                                UUID.randomUUID(),
                                List.of(new NucleoPatientDTO(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));
                InboundEnvelopeDTO<AcolhimentoCreatedDTO> createdEnvelopeDto = new InboundEnvelopeDTO<>(
                                eventId,
                                correlationId,
                                "humanizar-acolhimento",
                                "humanizar.acolhimento.event",
                                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                                "acolhimento",
                                aggregateId,
                                1,
                                LocalDateTime.now(),
                                actorId,
                                "JUnit",
                                "127.0.0.1",
                                createdPayload);

                when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
                                .thenReturn(envelopeDto, createdEnvelopeDto);
                when(processAcolhimentoCreatedUseCase.execute(any(), any(), any(), any()))
                                .thenReturn(EventOutcome.success());

                AcolhimentoConsumer consumer = new AcolhimentoConsumer(
                                objectMapper,
                                envelopeValidator,
                                processedEventGuard,
                                processingResultPublisher,
                                new InboundEnvelopeMapper(),
                                new AcolhimentoInboundMapper(),
                                processAcolhimentoCreatedUseCase,
                                processAcolhimentoUpdatedUseCase,
                                processAcolhimentoDeletedUseCase);

                MessageProperties properties = new MessageProperties();
                properties.setReceivedRoutingKey(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
                properties.setDeliveryTag(deliveryTag);
                Message message = new Message("{}".getBytes(), properties);

                consumer.onMessage(message, channel);

                verify(processAcolhimentoCreatedUseCase).execute(any(), eq(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1),
                                any(),
                                any());
                verify(processAcolhimentoUpdatedUseCase, never()).execute(any(), any(), any(), any());
                verify(processAcolhimentoDeletedUseCase, never()).execute(any(), any(), any(), any());
                verify(channel).basicAck(deliveryTag, false);
                verify(processingResultPublisher).publishProcessed(any(), eq(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1));
        }
}
