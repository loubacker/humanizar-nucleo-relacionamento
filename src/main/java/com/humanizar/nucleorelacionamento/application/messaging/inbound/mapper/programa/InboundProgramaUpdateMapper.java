package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class InboundProgramaUpdateMapper {

    private final ObjectMapper objectMapper;

    public InboundProgramaUpdateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ProgramaDTO> toPayload(InboundEnvelopeDTO<Object> envelope) {
        String correlationId = correlationIdAsString(envelope);
        List<ProgramaDTO> payload;

        try {
            payload = objectMapper.convertValue(envelope.payload(), new TypeReference<List<ProgramaDTO>>() {
            });
        } catch (IllegalArgumentException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_PARSE_ERROR,
                    correlationId,
                    "Falha ao mapear payload de programa.updated");
        }

        validate(payload, correlationId);
        return payload;
    }

    private void validate(List<ProgramaDTO> payload, String correlationId) {
        requireNotEmptyCollection(payload, "payload", correlationId);

        for (int i = 0; i < payload.size(); i++) {
            ProgramaDTO item = payload.get(i);
            String itemPath = "payload[" + i + "]";
            requireField(item, itemPath, correlationId);
            requireField(item.nucleoPatientId(), itemPath + ".nucleoPatientId", correlationId);
            requireNotEmptyCollection(item.abordagemId(), itemPath + ".abordagemId", correlationId);

            Set<UUID> uniqueAbordagens = new HashSet<>();
            for (int j = 0; j < item.abordagemId().size(); j++) {
                UUID abordagemId = item.abordagemId().get(j);
                requireField(abordagemId, itemPath + ".abordagemId[" + j + "]", correlationId);
                if (!uniqueAbordagens.add(abordagemId)) {
                    throw new NucleoRelacionamentoException(
                            ReasonCode.INBOUND_DUPLICATE_ITEM,
                            correlationId,
                            "Item duplicado: " + itemPath + ".abordagemId=" + abordagemId);
                }
            }
        }
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

    private void requireNotEmptyCollection(List<?> value, String fieldName, String correlationId) {
        if (value == null || value.isEmpty()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_EMPTY_COLLECTION,
                    correlationId,
                    "Colecao obrigatoria vazia: " + fieldName);
        }
    }
}
