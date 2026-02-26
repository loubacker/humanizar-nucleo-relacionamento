package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.InboundEnvelope;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class EnvelopeValidator {

    public void validate(InboundEnvelope<?> envelope) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;

        if (envelope.eventId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "event_id e obrigatorio");
        }
        if (envelope.correlationId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, null, "correlation_id e obrigatorio");
        }
        if (envelope.routingKey() == null || envelope.routingKey().isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "routing_key e obrigatorio");
        }
        if (envelope.eventVersion() < 1) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "event_version deve ser >= 1");
        }
        if (envelope.aggregateType() == null || envelope.aggregateType().isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "aggregate_type e obrigatorio");
        }
        if (envelope.aggregateId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "aggregate_id e obrigatorio");
        }
        if (envelope.actorId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "actor_id e obrigatorio");
        }
        if (envelope.userAgent() == null || envelope.userAgent().isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "user_agent e obrigatorio");
        }
        if (envelope.originIp() == null || envelope.originIp().isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "origin_ip e obrigatorio");
        }
        if (envelope.payload() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "payload e obrigatorio");
        }
    }
}
