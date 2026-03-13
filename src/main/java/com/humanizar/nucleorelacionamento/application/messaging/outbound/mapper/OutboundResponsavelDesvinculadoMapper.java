package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelDesvinculadoDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class OutboundResponsavelDesvinculadoMapper {

    private static final String PRODUCER_SERVICE = "humanizar-nucleo-relacionamento";

    public OutboundEnvelopeDTO<OutboundResponsavelDesvinculadoDTO> toEnvelope(
            UUID eventId,
            UUID correlationId,
            UUID actorId,
            String userAgent,
            String originIp,
            OutboundResponsavelDesvinculadoDTO payload) {

        String correlationAsString = correlationId != null ? correlationId.toString() : null;
        requireNonNull(eventId, "eventId", correlationAsString);
        requireNonNull(correlationId, "correlationId", correlationAsString);
        requireText(PRODUCER_SERVICE, "producerService", correlationAsString);
        requireNonNull(payload, "payload", correlationAsString);

        LocalDateTime occurredAt = LocalDateTime.now();
        requireNonNull(occurredAt, "occurredAt", correlationAsString);

        return new OutboundEnvelopeDTO<>(
                eventId,
                correlationId,
                PRODUCER_SERVICE,
                occurredAt,
                actorId,
                userAgent,
                originIp,
                payload);
    }

    private <T> void requireNonNull(T value, String fieldName, String correlationId) {
        if (value == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }

    private void requireText(String value, String fieldName, String correlationId) {
        if (value == null || value.isBlank()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }
}
