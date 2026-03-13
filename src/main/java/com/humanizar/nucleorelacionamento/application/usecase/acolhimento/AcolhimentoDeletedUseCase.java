package com.humanizar.nucleorelacionamento.application.usecase.acolhimento;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.service.NucleoPatientService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.ProcessedEvent;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.ProcessedEventPort;

@Component
public class AcolhimentoDeletedUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcolhimentoDeletedUseCase.class);

    private final NucleoPatientService nucleoPatientService;
    private final ProcessedEventPort processedEventPort;

    public AcolhimentoDeletedUseCase(
            NucleoPatientService nucleoPatientService,
            ProcessedEventPort processedEventPort) {
        this.nucleoPatientService = nucleoPatientService;
        this.processedEventPort = processedEventPort;
    }

    public EventOutcome execute(
            String consumerName,
            String routingKey,
            InboundEnvelopeDTO<?> envelope,
            AcolhimentoDeletedDTO command) {

        try {
            nucleoPatientService.deleteAllNucleosByPatientId(
                    command.patientId(),
                    envelope.correlationId(),
                    envelope.actorId(),
                    envelope.userAgent(),
                    envelope.originIp());

            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    routingKey,
                    ProcessedResult.SUCCESS,
                    null,
                    null));
            return EventOutcome.success();

        } catch (DataIntegrityViolationException ex) {
            log.info("DataIntegrityViolation no processamento de delatar -- possível duplicata. consumer={}, eventId={}",
                    consumerName, envelope.eventId());
            return EventOutcome.ignored(ReasonCode.DUPLICATE_EVENT);

        } catch (NucleoRelacionamentoException ex) {
            return handleDomainException(ex, consumerName, routingKey, envelope);

        } catch (Exception ex) {
            return handleUnexpectedException(ex, consumerName, routingKey, envelope);
        }
    }

    private EventOutcome handleDomainException(
            NucleoRelacionamentoException ex,
            String consumerName,
            String routingKey,
            InboundEnvelopeDTO<?> envelope) {

        ReasonCode reason = ex.getReasonCode() != null
                ? ex.getReasonCode()
                : ReasonCode.VALIDATION_ERROR;

        if (reason == ReasonCode.DUPLICATE_EVENT) {
            log.info("Evento duplicado ignorado. eventId={}, correlationId={}, consumer={}",
                    envelope.eventId(), envelope.correlationId(), consumerName);
            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    routingKey,
                    ProcessedResult.IGNORED,
                    reason,
                    reason.name().toLowerCase()));
            return EventOutcome.ignored(reason);
        }

        if (reason.isRetryable()) {
            log.error("Falha retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                    reason, envelope.eventId(), envelope.correlationId(), consumerName, ex);
            return EventOutcome.failed(reason);
        }

        log.warn("Falha funcional nao retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                reason, envelope.eventId(), envelope.correlationId(), consumerName);
        saveProcessedEvent(buildProcessedEvent(
                consumerName,
                envelope,
                routingKey,
                ProcessedResult.FAILED,
                reason,
                reason.name().toLowerCase()));
        return EventOutcome.failed(reason);
    }

    private EventOutcome handleUnexpectedException(
            Exception ex,
            String consumerName,
            String routingKey,
            InboundEnvelopeDTO<?> envelope) {

        if (ex instanceof IllegalArgumentException) {
            String detail = "inbound_invalid_enum: " + ex.getMessage();
            log.warn("IllegalArgumentException tratada como INBOUND_INVALID_ENUM. eventId={}, correlationId={}, consumer={}",
                    envelope.eventId(), envelope.correlationId(), consumerName, ex);
            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    routingKey,
                    ProcessedResult.FAILED,
                    ReasonCode.INBOUND_INVALID_ENUM,
                    detail));
            return EventOutcome.failed(ReasonCode.INBOUND_INVALID_ENUM, detail);
        }

        log.error("Excecao inesperada. eventId={}, correlationId={}, consumer={}, eventType={}, aggregateType={}, aggregateId={}",
                envelope.eventId(), envelope.correlationId(), consumerName,
                routingKey, envelope.aggregateType(), envelope.aggregateId(), ex);

        String exceptionType = ex != null ? ex.getClass().getSimpleName() : "UnknownException";
        return EventOutcome.failed(ReasonCode.PERSISTENCE_FAILURE,
                "unexpected_error: " + exceptionType);
    }

    private ProcessedEvent buildProcessedEvent(
            String consumerName,
            InboundEnvelopeDTO<?> envelope,
            String routingKey,
            ProcessedResult result,
            ReasonCode reasonCode,
            String errorMessage) {
        return ProcessedEvent.builder()
                .consumerName(consumerName)
                .eventId(envelope.eventId())
                .correlationId(envelope.correlationId())
                .eventType(routingKey)
                .aggregateType(envelope.aggregateType())
                .aggregateId(envelope.aggregateId())
                .actorId(envelope.actorId())
                .userAgent(envelope.userAgent())
                .originIp(envelope.originIp())
                .processedAt(LocalDateTime.now())
                .result(result)
                .reasonCode(reasonCode)
                .errorMessage(errorMessage)
                .build();
    }

    private void saveProcessedEvent(ProcessedEvent event) {
        try {
            processedEventPort.save(event);
        } catch (DataIntegrityViolationException ex) {
            log.info("Evento duplicado detectado via constraint ao gravar processed_event. consumer={}, eventId={}. Ignorando.",
                    event.getConsumerName(), event.getEventId());
        }
    }
}
