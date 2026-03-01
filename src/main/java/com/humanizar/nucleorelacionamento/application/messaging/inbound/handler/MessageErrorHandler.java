package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.ProcessedEvent;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.ProcessedEventPort;

@Component
public class MessageErrorHandler {

        private static final Logger log = LoggerFactory.getLogger(MessageErrorHandler.class);

        private final ProcessedEventPort processedEventPort;

        public MessageErrorHandler(ProcessedEventPort processedEventPort) {
                this.processedEventPort = processedEventPort;
        }

        public EventOutcome handle(String consumerName,
                        UUID eventId,
                        UUID correlationId,
                        String eventType,
                        String aggregateType,
                        UUID aggregateId,
                        UUID actorId, String userAgent, String originIp,
                        Supplier<Void> useCaseAction) {

                try {
                        useCaseAction.get();
                        recordProcessedEvent(consumerName, eventId, correlationId,
                                        eventType, aggregateType, aggregateId,
                                        actorId, userAgent, originIp,
                                        ProcessedResult.SUCCESS, null, null);
                        return EventOutcome.success();

                } catch (DataIntegrityViolationException ex) {
                        log.info("DataIntegrityViolation no processamento -- possivel duplicata. consumer={}, eventId={}",
                                        consumerName, eventId);
                        return EventOutcome.ignored(ReasonCode.DUPLICATE_EVENT);

                } catch (NucleoRelacionamentoException ex) {
                        return handleDomainException(ex, consumerName, eventId,
                                        correlationId, eventType, aggregateType, aggregateId,
                                        actorId, userAgent, originIp);

                } catch (Exception ex) {
                        return handleUnexpectedException(ex, consumerName, eventId,
                                        correlationId, eventType, aggregateType, aggregateId,
                                        actorId, userAgent, originIp);
                }
        }

        private EventOutcome handleDomainException(
                        NucleoRelacionamentoException ex,
                        String consumerName, UUID eventId, UUID correlationId,
                        String eventType, String aggregateType, UUID aggregateId,
                        UUID actorId, String userAgent, String originIp) {

                ReasonCode reason = ex.getReasonCode();

                if (reason == ReasonCode.DUPLICATE_EVENT) {
                        log.info("Evento duplicado ignorado. eventId={}, correlationId={}, consumer={}",
                                        eventId, correlationId, consumerName);
                        recordProcessedEvent(consumerName, eventId, correlationId,
                                        eventType, aggregateType, aggregateId,
                                        actorId, userAgent, originIp,
                                        ProcessedResult.IGNORED, reason, reason.name().toLowerCase());
                        return EventOutcome.ignored(reason);
                }

                if (reason.isRetryable()) {
                        log.error("Falha retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                                        reason, eventId, correlationId, consumerName, ex);
                        return EventOutcome.failed(reason);
                }

                log.warn("Falha funcional nao retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                                reason, eventId, correlationId, consumerName);
                recordProcessedEvent(consumerName, eventId, correlationId,
                                eventType, aggregateType, aggregateId,
                                actorId, userAgent, originIp,
                                ProcessedResult.FAILED, reason, reason.name().toLowerCase());
                return EventOutcome.failed(reason);
        }

        private EventOutcome handleUnexpectedException(
                        Exception ex,
                        String consumerName, UUID eventId, UUID correlationId,
                        String eventType, String aggregateType, UUID aggregateId,
                        UUID actorId, String userAgent, String originIp) {

                if (ex instanceof IllegalArgumentException) {
                        log.warn("IllegalArgumentException tratada como VALIDATION_ERROR. eventId={}, correlationId={}, consumer={}",
                                        eventId, correlationId, consumerName, ex);
                        recordProcessedEvent(consumerName, eventId, correlationId,
                                        eventType, aggregateType, aggregateId,
                                        actorId, userAgent, originIp,
                                        ProcessedResult.FAILED, ReasonCode.VALIDATION_ERROR,
                                        "validation_error: " + ex.getMessage());
                        return EventOutcome.failed(ReasonCode.VALIDATION_ERROR,
                                        "validation_error: " + ex.getMessage());
                }

                log.error("Excecao inesperada. eventId={}, correlationId={}, consumer={}, eventType={}, aggregateType={}, aggregateId={}",
                                eventId, correlationId, consumerName, eventType, aggregateType, aggregateId, ex);
                String exceptionType = ex != null ? ex.getClass().getSimpleName() : "UnknownException";
                return EventOutcome.failed(ReasonCode.PERSISTENCE_FAILURE,
                                "unexpected_error: " + exceptionType);
        }

        private void recordProcessedEvent(
                        String consumerName, UUID eventId, UUID correlationId,
                        String eventType, String aggregateType, UUID aggregateId,
                        UUID actorId, String userAgent, String originIp,
                        ProcessedResult result, ReasonCode reasonCode, String errorMessage) {

                ProcessedEvent event = ProcessedEvent.builder()
                                .consumerName(consumerName)
                                .eventId(eventId)
                                .correlationId(correlationId)
                                .eventType(eventType)
                                .aggregateType(aggregateType)
                                .aggregateId(aggregateId)
                                .actorId(actorId)
                                .userAgent(userAgent)
                                .originIp(originIp)
                                .processedAt(LocalDateTime.now())
                                .result(result)
                                .reasonCode(reasonCode)
                                .errorMessage(errorMessage)
                                .build();

                try {
                        processedEventPort.save(event);
                } catch (DataIntegrityViolationException ex) {
                        log.info("Evento duplicado detectado via constraint ao gravar processed_event. consumer={}, eventId={}. Ignorando.",
                                        consumerName, eventId);
                }
        }
}
