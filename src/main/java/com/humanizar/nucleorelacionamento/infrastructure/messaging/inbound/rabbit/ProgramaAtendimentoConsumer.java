package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaItemDTO;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.mapper.ProgramaInboundMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.EventMetadata;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.InboundEnvelope;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaUpdatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.EnvelopeValidator;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.ProgramaPayloadValidator;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.application.service.AbordagemPatientService;
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
    private static final String CONSUMER_NAME = "programa-atendimento-consumer";

    private final ObjectMapper objectMapper;
    private final EnvelopeValidator envelopeValidator;
    private final ProgramaPayloadValidator payloadValidator;
    private final ProcessedEventGuard processedEventGuard;
    private final MessageErrorHandler messageErrorHandler;
    private final ProcessingResultPublisher processingResultPublisher;
    private final AbordagemPatientService abordagemPatientService;
    private final InboundEnvelopeMapper inboundEnvelopeMapper;
    private final ProgramaInboundMapper programaInboundMapper;

    public ProgramaAtendimentoConsumer(ObjectMapper objectMapper,
            EnvelopeValidator envelopeValidator,
            ProgramaPayloadValidator payloadValidator,
            ProcessedEventGuard processedEventGuard,
            MessageErrorHandler messageErrorHandler,
            ProcessingResultPublisher processingResultPublisher,
            AbordagemPatientService abordagemPatientService,
            InboundEnvelopeMapper inboundEnvelopeMapper,
            ProgramaInboundMapper programaInboundMapper) {
        this.objectMapper = objectMapper;
        this.envelopeValidator = envelopeValidator;
        this.payloadValidator = payloadValidator;
        this.processedEventGuard = processedEventGuard;
        this.messageErrorHandler = messageErrorHandler;
        this.processingResultPublisher = processingResultPublisher;
        this.abordagemPatientService = abordagemPatientService;
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
        this.programaInboundMapper = programaInboundMapper;
    }

    @RabbitListener(queues = QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        InboundEnvelope<Object> envelope = null;
        String routingKey = null;

        try {
            byte[] body = message.getBody();
            routingKey = message.getMessageProperties().getReceivedRoutingKey();

            InboundEnvelopeDTO<Object> envelopeDto = parseEnvelope(body, new TypeReference<InboundEnvelopeDTO<Object>>() {
            });
            envelope = inboundEnvelopeMapper.toCommandEnvelope(envelopeDto);
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

    private void tryPublishEarlyRejection(InboundEnvelope<Object> envelope, String routingKey,
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
            InboundEnvelope<Object> envelope) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;
        EventMetadata meta = EventMetadata.fromEnvelope(envelope);

        return switch (routingKey) {
            case RoutingKeyCatalog.PROGRAMA_CREATED_V1 -> {
                InboundEnvelopeDTO<List<ProgramaItemDTO>> createdEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<List<ProgramaItemDTO>>>() {
                        });
                ProgramaCreatedCommand createdCommand = programaInboundMapper.toCreatedCommand(createdEnvelopeDto.payload());
                payloadValidator.validateCreated(createdCommand, correlationId);

                yield messageErrorHandler.handle(CONSUMER_NAME, envelope.eventId(),
                        envelope.correlationId(), routingKey,
                        envelope.aggregateType(), envelope.aggregateId(),
                        meta.actorId(), meta.userAgent(), meta.originIp(),
                        () -> {
                            abordagemPatientService.createAbordagens(
                                    createdCommand.nucleoAbordagens(), envelope.correlationId());
                            return null;
                        });
            }
            case RoutingKeyCatalog.PROGRAMA_UPDATED_V1 -> {
                InboundEnvelopeDTO<List<ProgramaItemDTO>> updatedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<List<ProgramaItemDTO>>>() {
                        });
                ProgramaUpdatedCommand updatedCommand = programaInboundMapper.toUpdatedCommand(updatedEnvelopeDto.payload());
                payloadValidator.validateUpdated(updatedCommand, correlationId);

                yield messageErrorHandler.handle(CONSUMER_NAME, envelope.eventId(),
                        envelope.correlationId(), routingKey,
                        envelope.aggregateType(), envelope.aggregateId(),
                        meta.actorId(), meta.userAgent(), meta.originIp(),
                        () -> {
                            abordagemPatientService.reconcileAbordagens(
                                    updatedCommand.nucleoAbordagens(), envelope.correlationId());
                            return null;
                        });
            }
            case RoutingKeyCatalog.PROGRAMA_DELETED_V1 -> {
                InboundEnvelopeDTO<ProgramaDeletedDTO> deletedEnvelopeDto = parseEnvelope(body,
                        new TypeReference<InboundEnvelopeDTO<ProgramaDeletedDTO>>() {
                        });
                ProgramaDeletedCommand deletedCommand = programaInboundMapper.toDeletedCommand(deletedEnvelopeDto.payload());
                payloadValidator.validateDeleted(deletedCommand, correlationId);

                yield messageErrorHandler.handle(CONSUMER_NAME, envelope.eventId(),
                        envelope.correlationId(), routingKey,
                        envelope.aggregateType(), envelope.aggregateId(),
                        meta.actorId(), meta.userAgent(), meta.originIp(),
                        () -> {
                            abordagemPatientService.deleteAllAbordagensByPatientId(
                                    deletedCommand.patientId(), envelope.correlationId());
                            return null;
                        });
            }
            default -> {
                log.warn("Routing key nao suportada: {}", routingKey);
                throw new NucleoRelacionamentoException(
                        ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId);
            }
        };
    }

    private void publishProcessingResult(String upstreamRoutingKey,
            InboundEnvelope<Object> inboundEnvelope,
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
