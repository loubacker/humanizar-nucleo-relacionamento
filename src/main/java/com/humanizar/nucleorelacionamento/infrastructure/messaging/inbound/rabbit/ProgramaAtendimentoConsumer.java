package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.mapper.ProgramaInboundMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.EnvelopeValidator;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaCreatedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaDeletedUseCase;
import com.humanizar.nucleorelacionamento.application.usecase.inbound.programa.ProgramaUpdatedUseCase;
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
import java.util.List;

@Component
public class ProgramaAtendimentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProgramaAtendimentoConsumer.class);

    private final ObjectMapper objectMapper;
    private final EnvelopeValidator envelopeValidator;
    private final ProcessedEventGuard processedEventGuard;
    private final ProcessingResultPublisher processingResultPublisher;
    private final InboundEnvelopeMapper inboundEnvelopeMapper;
    private final ProgramaInboundMapper programaInboundMapper;
    private final ProgramaCreatedUseCase programaCreatedUseCase;
    private final ProgramaUpdatedUseCase programaUpdatedUseCase;
    private final ProgramaDeletedUseCase programaDeletedUseCase;

    public ProgramaAtendimentoConsumer(ObjectMapper objectMapper,
            EnvelopeValidator envelopeValidator,
            ProcessedEventGuard processedEventGuard,
            ProcessingResultPublisher processingResultPublisher,
            InboundEnvelopeMapper inboundEnvelopeMapper,
            ProgramaInboundMapper programaInboundMapper,
            ProgramaCreatedUseCase programaCreatedUseCase,
            ProgramaUpdatedUseCase programaUpdatedUseCase,
            ProgramaDeletedUseCase programaDeletedUseCase) {
        this.objectMapper = objectMapper;
        this.envelopeValidator = envelopeValidator;
        this.processedEventGuard = processedEventGuard;
        this.processingResultPublisher = processingResultPublisher;
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
        this.programaInboundMapper = programaInboundMapper;
        this.programaCreatedUseCase = programaCreatedUseCase;
        this.programaUpdatedUseCase = programaUpdatedUseCase;
        this.programaDeletedUseCase = programaDeletedUseCase;
    }

    @RabbitListener(queues = QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA, containerFactory = "rabbitListenerContainerFactory")
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

            processedEventGuard.ensureNotProcessed(ConsumerCatalog.PROGRAMA_CONSUMER, envelope.eventId(),
                    correlationId);

            EventOutcome outcome = dispatchByRoutingKey(routingKey, body, envelope);
            publishProcessingResult(routingKey, envelope, outcome);

            if (outcome.retryable()) {
                channel.basicNack(deliveryTag, false, true);
            } else {
                channel.basicAck(deliveryTag, false);
            }

        } catch (NucleoRelacionamentoException ex) {
            if (ex.isRetryable()) {
                log.error("Erro retentavel no consumer programa. reason={}", ex.getReasonCode(), ex);
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.warn("Erro nao retentavel no consumer programa. reason={}", ex.getReasonCode());
                tryPublishEarlyRejection(envelope, routingKey, ex);
                channel.basicAck(deliveryTag, false);
            }
        } catch (RuntimeException | IOException ex) {
            log.error("Erro inesperado no consumer programa.", ex);
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
            log.error("Falha ao publicar rejeicao pre-dispatch no consumer programa.", nested);
        }
    }

    private EventOutcome dispatchByRoutingKey(String routingKey, byte[] body,
            InboundEnvelopeDTO<Object> envelope) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;

        return switch (routingKey) {
            case RoutingKeyCatalog.PROGRAMA_CREATED_V1 -> {
                InboundEnvelopeDTO<List<ProgramaDTO>> createdEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<List<ProgramaDTO>>>() {
                        });
                List<ProgramaDTO> createdPayload = programaInboundMapper
                        .toCreatedPayload(createdEnvelopeDto.payload());
                yield programaCreatedUseCase.execute(
                        ConsumerCatalog.PROGRAMA_CONSUMER,
                        routingKey,
                        envelope,
                        createdPayload);
            }
            case RoutingKeyCatalog.PROGRAMA_UPDATED_V1 -> {
                InboundEnvelopeDTO<List<ProgramaDTO>> updatedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<List<ProgramaDTO>>>() {
                        });
                List<ProgramaDTO> updatedPayload = programaInboundMapper
                        .toUpdatedPayload(updatedEnvelopeDto.payload());
                yield programaUpdatedUseCase.execute(
                        ConsumerCatalog.PROGRAMA_CONSUMER,
                        routingKey,
                        envelope,
                        updatedPayload);
            }
            case RoutingKeyCatalog.PROGRAMA_DELETED_V1 -> {
                InboundEnvelopeDTO<ProgramaDeletedDTO> deletedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<ProgramaDeletedDTO>>() {
                        });
                ProgramaDeletedDTO deletedPayload = programaInboundMapper
                        .toDeletedPayload(deletedEnvelopeDto.payload());
                yield programaDeletedUseCase.execute(
                        ConsumerCatalog.PROGRAMA_CONSUMER,
                        routingKey,
                        envelope,
                        deletedPayload);
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
