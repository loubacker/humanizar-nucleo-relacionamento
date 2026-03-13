package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class InboundProgramaDeleteMapper {

    private final ObjectMapper objectMapper;

    public InboundProgramaDeleteMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProgramaDeletedDTO toPayload(InboundEnvelopeDTO<Object> envelope) {
        String correlationId = correlationIdAsString(envelope);
        ProgramaDeletedDTO payload;

        try {
            payload = objectMapper.convertValue(envelope.payload(), ProgramaDeletedDTO.class);
        } catch (IllegalArgumentException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_PARSE_ERROR,
                    correlationId,
                    "Falha ao mapear payload de programa.deleted");
        }

        requireField(payload, "payload", correlationId);
        requireField(payload.patientId(), "payload.patientId", correlationId);
        return payload;
    }

    private String correlationIdAsString(InboundEnvelopeDTO<?> envelope) {
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
}
