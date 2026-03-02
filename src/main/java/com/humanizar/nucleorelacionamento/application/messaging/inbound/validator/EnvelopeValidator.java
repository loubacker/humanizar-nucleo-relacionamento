package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class EnvelopeValidator {

    public void validate(InboundEnvelopeDTO<?> envelope) {
        requireNotNull(envelope, "envelope é obrigatorio", null);
        String correlationId = getCorrelationId(envelope);

        requireNotNull(envelope.eventId(), "event_id é obrigatorio", correlationId);
        requireNotNull(envelope.correlationId(), "correlation_id é obrigatorio", null);
        requireNotBlank(envelope.routingKey(), "routing_key é obrigatorio", correlationId);
        requireTrue(envelope.eventVersion() >= 1, "event_version deve ser >= 1", correlationId);
        requireNotBlank(envelope.aggregateType(), "aggregate_type é obrigatorio", correlationId);
        requireNotNull(envelope.aggregateId(), "aggregate_id é obrigatorio", correlationId);
        requireNotNull(envelope.actorId(), "actor_id é obrigatorio", correlationId);
        requireNotBlank(envelope.userAgent(), "user_agent é obrigatorio", correlationId);
        requireNotBlank(envelope.originIp(), "origin_ip é obrigatorio", correlationId);
        requireNotNull(envelope.payload(), "payload é obrigatorio", correlationId);
    }

    private String getCorrelationId(InboundEnvelopeDTO<?> envelope) {
        return envelope.correlationId() != null ? envelope.correlationId().toString() : null;
    }

    private void requireNotNull(Object value, String message, String correlationId) {
        requireTrue(value != null, message, correlationId);
    }

    private void requireNotBlank(String value, String message, String correlationId) {
        requireTrue(value != null && !value.isBlank(), message, correlationId);
    }

    private void requireTrue(boolean condition, String message, String correlationId) {
        if (!condition) {
            throw new NucleoRelacionamentoException(ReasonCode.VALIDATION_ERROR, correlationId, message);
        }
    }
}
