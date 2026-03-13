package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.CallbackDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class OutboundCallbackMapper {

    private static final String PRODUCER_SERVICE = "humanizar-nucleo-relacionamento";

    public CallbackDTO toProcessedCallback(
            InboundEnvelopeDTO<?> inboundEnvelope,
            String upStreamRoutingKey,
            String callbackExchange,
            String callbackRoutingKey) {
        LocalDateTime now = LocalDateTime.now();
        return toCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                callbackRoutingKey,
                "PROCESSED",
                null,
                null,
                now,
                null,
                now);
    }

    public CallbackDTO toRejectedCallback(
            InboundEnvelopeDTO<?> inboundEnvelope,
            String upStreamRoutingKey,
            String callbackExchange,
            String callbackRoutingKey,
            String reasonCode,
            String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        return toCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                callbackRoutingKey,
                "REJECTED",
                reasonCode,
                errorMessage,
                null,
                now,
                now);
    }

    private CallbackDTO toCallback(
            InboundEnvelopeDTO<?> inboundEnvelope,
            String upStreamRoutingKey,
            String callbackExchange,
            String callbackRoutingKey,
            String status,
            String reasonCode,
            String errorMessage,
            LocalDateTime processedAt,
            LocalDateTime rejectedAt,
            LocalDateTime occurredAt) {

        String correlationId = inboundEnvelope != null && inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;

        InboundEnvelopeDTO<?> envelope = requireEnvelope(inboundEnvelope, correlationId);
        requireText(upStreamRoutingKey, "upStreamRoutingKey", correlationId);
        requireText(callbackExchange, "callbackExchange", correlationId);
        requireText(callbackRoutingKey, "callbackRoutingKey", correlationId);
        requireText(status, "status", correlationId);
        requireText(PRODUCER_SERVICE, "producerService", correlationId);
        requireNonNull(envelope.eventId(), "eventId", correlationId);
        requireNonNull(envelope.correlationId(), "correlationId", correlationId);
        requireText(envelope.aggregateType(), "aggregateType", correlationId);
        requireNonNull(envelope.aggregateId(), "aggregateId", correlationId);
        requireNonNull(occurredAt, "occurredAt", correlationId);

        return new CallbackDTO(
                upStreamRoutingKey,
                envelope.eventId(),
                envelope.correlationId(),
                PRODUCER_SERVICE,
                callbackExchange,
                callbackRoutingKey,
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.eventVersion(),
                occurredAt,
                envelope.actorId(),
                envelope.userAgent(),
                envelope.originIp(),
                status,
                reasonCode,
                errorMessage,
                processedAt,
                rejectedAt);
    }

    private InboundEnvelopeDTO<?> requireEnvelope(InboundEnvelopeDTO<?> value, String correlationId) {
        if (value == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo obrigatorio ausente: inboundEnvelope");
        }
        return value;
    }

    private void requireText(String value, String fieldName, String correlationId) {
        if (value == null || value.isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }

    private <T> void requireNonNull(T value, String fieldName, String correlationId) {
        if (value == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }
}
