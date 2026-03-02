package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.application.mapper.AcolhimentoInboundMapper;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.EnvelopeValidator;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento.AcolhimentoUpdatedUseCase;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AcolhimentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AcolhimentoConsumer.class);
    private static final String CONSUMER_NAME = "acolhimento-consumer";

    private final ObjectMapper objectMapper;
    private final EnvelopeValidator envelopeValidator;
    private final ProcessedEventGuard processedEventGuard;
    private final ProcessingResultPublisher processingResultPublisher;
    private final InboundEnvelopeMapper inboundEnvelopeMapper;
    private final AcolhimentoInboundMapper acolhimentoInboundMapper;
    private final AcolhimentoCreatedUseCase processAcolhimentoCreatedUseCase;
    private final AcolhimentoUpdatedUseCase processAcolhimentoUpdatedUseCase;
    private final AcolhimentoDeletedUseCase processAcolhimentoDeletedUseCase;

    public AcolhimentoConsumer(ObjectMapper objectMapper,
            EnvelopeValidator envelopeValidator,
            ProcessedEventGuard processedEventGuard,
            ProcessingResultPublisher processingResultPublisher,
            InboundEnvelopeMapper inboundEnvelopeMapper,
            AcolhimentoInboundMapper acolhimentoInboundMapper,
            AcolhimentoCreatedUseCase processAcolhimentoCreatedUseCase,
            AcolhimentoUpdatedUseCase processAcolhimentoUpdatedUseCase,
            AcolhimentoDeletedUseCase processAcolhimentoDeletedUseCase) {
        this.objectMapper = objectMapper;
        this.envelopeValidator = envelopeValidator;
        this.processedEventGuard = processedEventGuard;
        this.processingResultPublisher = processingResultPublisher;
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
        this.acolhimentoInboundMapper = acolhimentoInboundMapper;
        this.processAcolhimentoCreatedUseCase = processAcolhimentoCreatedUseCase;
        this.processAcolhimentoUpdatedUseCase = processAcolhimentoUpdatedUseCase;
        this.processAcolhimentoDeletedUseCase = processAcolhimentoDeletedUseCase;
    }

    @RabbitListener(queues = QueueCatalog.NUCLEO_RELACIONAMENTO_ACOLHIMENTO, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        InboundEnvelopeDTO<Object> envelope = null;
        String routingKey = null;

        try {
            byte[] body = message.getBody();
            routingKey = message.getMessageProperties().getReceivedRoutingKey();

            InboundEnvelopeDTO<Object> envelopeDto = parseEnvelope(body,
                    new TypeReference<InboundEnvelopeDTO<Object>>() {
                    });
            envelope = inboundEnvelopeMapper.toInboundEnvelope(envelopeDto);
            envelopeValidator.validate(envelope);

            String correlationId = envelope.correlationId() != null
                    ? envelope.correlationId().toString()
                    : null;

            processedEventGuard.ensureNotProcessed(CONSUMER_NAME, envelope.eventId(), correlationId);

            EventOutcome outcome = dispatchByRoutingKey(routingKey, body, envelope);
            publishProcessingResult(routingKey, envelope, outcome);

            if (outcome.retryable()) {
                channel.basicNack(deliveryTag, false, true);
            } else {
                channel.basicAck(deliveryTag, false);
            }

        } catch (NucleoRelacionamentoException ex) {
            if (ex.isRetryable()) {
                log.error("Erro retentavel no consumer acolhimento. reason={}", ex.getReasonCode(), ex);
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.warn("Erro nao retentavel no consumer acolhimento. reason={}", ex.getReasonCode());
                tryPublishEarlyRejection(envelope, routingKey, ex);
                channel.basicAck(deliveryTag, false);
            }
        } catch (RuntimeException | IOException ex) {
            log.error("Erro inesperado no consumer acolhimento.", ex);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void tryPublishEarlyRejection(InboundEnvelopeDTO<Object> envelope, String routingKey,
            NucleoRelacionamentoException ex) {
        if (envelope == null || routingKey == null || ex.getReasonCode() == ReasonCode.DUPLICATE_EVENT) {
            return;
        }
        try {
            EventOutcome rejection = EventOutcome.failed(ex.getReasonCode());
            publishProcessingResult(routingKey, envelope, rejection);
        } catch (Exception nested) {
            log.error("Falha ao publicar rejeicao pre-dispatch no consumer acolhimento.", nested);
        }
    }

    private EventOutcome dispatchByRoutingKey(String routingKey, byte[] body,
            InboundEnvelopeDTO<Object> envelope) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;

        return switch (routingKey) {
            case RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1 -> {
                InboundEnvelopeDTO<AcolhimentoCreatedDTO> createdEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<AcolhimentoCreatedDTO>>() {
                        });
                AcolhimentoCreatedDTO createdPayload = acolhimentoInboundMapper
                        .toCreatedPayload(createdEnvelopeDto.payload());
                yield processAcolhimentoCreatedUseCase.execute(
                        CONSUMER_NAME, routingKey, envelope, createdPayload);
            }
            case RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1 -> {
                InboundEnvelopeDTO<AcolhimentoUpdatedDTO> updatedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<AcolhimentoUpdatedDTO>>() {
                        });
                AcolhimentoUpdatedDTO updatedPayload = acolhimentoInboundMapper
                        .toUpdatedPayload(updatedEnvelopeDto.payload());
                yield processAcolhimentoUpdatedUseCase.execute(
                        CONSUMER_NAME, routingKey, envelope, updatedPayload);
            }
            case RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1 -> {
                InboundEnvelopeDTO<AcolhimentoDeletedDTO> deletedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<AcolhimentoDeletedDTO>>() {
                        });
                AcolhimentoDeletedDTO deletedPayload = acolhimentoInboundMapper
                        .toDeletedPayload(deletedEnvelopeDto.payload());
                yield processAcolhimentoDeletedUseCase.execute(
                        CONSUMER_NAME, routingKey, envelope, deletedPayload);
            }
            default -> {
                log.warn("Routing key nao suportada: {}", routingKey);
                throw new NucleoRelacionamentoException(
                        ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId);
            }
        };
    }

    private void publishProcessingResult(String upstreamRoutingKey,
            InboundEnvelopeDTO<Object> inboundEnvelope,
            EventOutcome eventOutcome) {
        if (eventOutcome.result() == ProcessedResult.SUCCESS) {
            processingResultPublisher.publishProcessed(inboundEnvelope, upstreamRoutingKey);
            return;
        }
        if (eventOutcome.result() == ProcessedResult.FAILED && !eventOutcome.retryable()) {
            processingResultPublisher.publishRejected(inboundEnvelope, upstreamRoutingKey, eventOutcome);
        }
    }

    private <T> InboundEnvelopeDTO<T> parseEnvelope(byte[] body, TypeReference<InboundEnvelopeDTO<T>> typeRef) {
        try {
            return objectMapper.readValue(body, typeRef);
        } catch (IOException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, null, "Falha ao parsear envelope: " + ex.getMessage());
        }
    }
}
