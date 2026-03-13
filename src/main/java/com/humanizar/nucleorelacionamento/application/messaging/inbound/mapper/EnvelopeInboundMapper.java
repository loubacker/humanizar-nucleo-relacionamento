package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class EnvelopeInboundMapper {

    private final ObjectMapper objectMapper;

    public EnvelopeInboundMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public InboundEnvelopeDTO<Object> parseEnvelope(byte[] body) {
        try {
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_PARSE_ERROR,
                    null,
                    "Falha ao parsear envelope inbound");
        }
    }

    public void validate(InboundEnvelopeDTO<?> envelope) {
        requireField(envelope, "envelope", null);

        String correlationId = correlationIdAsString(envelope);

        requireField(envelope.eventId(), "eventId", correlationId);
        requireField(envelope.correlationId(), "correlationId", null);
        requireNotBlank(envelope.producerService(), "producerService", correlationId);
        requireNotBlank(envelope.exchangeName(), "exchangeName", correlationId);
        requireNotBlank(envelope.routingKey(), "routingKey", correlationId);
        requireNotBlank(envelope.aggregateType(), "aggregateType", correlationId);
        requireField(envelope.aggregateId(), "aggregateId", correlationId);
        requireField(envelope.occurredAt(), "occurredAt", correlationId);
        requireField(envelope.actorId(), "actorId", correlationId);
        requireNotBlank(envelope.userAgent(), "userAgent", correlationId);
        requireNotBlank(envelope.originIp(), "originIp", correlationId);
        requireField(envelope.payload(), "payload", correlationId);

        if (envelope.eventVersion() < 1) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo invalido: eventVersion deve ser >= 1");
        }
    }

    public String correlationIdAsString(InboundEnvelopeDTO<?> envelope) {
        if (envelope == null || envelope.correlationId() == null) {
            return null;
        }
        return envelope.correlationId().toString();
    }

    private void requireField(Object value, String fieldName, String correlationId) {
        if (value == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }

    private void requireNotBlank(String value, String fieldName, String correlationId) {
        if (value == null || value.isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }
}
