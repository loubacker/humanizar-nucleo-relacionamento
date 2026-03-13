package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;

@Component
public class InboundAcolhimentoCreateMapper {

    private final ObjectMapper objectMapper;

    public InboundAcolhimentoCreateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AcolhimentoCreatedDTO toPayload(InboundEnvelopeDTO<Object> envelope) {
        String correlationId = correlationIdAsString(envelope);
        AcolhimentoCreatedDTO payload;

        try {
            payload = objectMapper.convertValue(envelope.payload(), AcolhimentoCreatedDTO.class);
        } catch (IllegalArgumentException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_PARSE_ERROR,
                    correlationId,
                    "Falha ao mapear payload de acolhimento.created");
        }

        requireField(payload, "payload", correlationId);
        requireField(payload.patientId(), "payload.patientId", correlationId);
        validateNucleoPatients(payload.nucleoPatient(), correlationId);
        return payload;
    }

    private void validateNucleoPatients(List<NucleoPatientDTO> nucleoPatient, String correlationId) {
        requireNotEmptyCollection(nucleoPatient, "payload.nucleoPatient", correlationId);

        Set<UUID> uniqueIds = new HashSet<>();
        for (int i = 0; i < nucleoPatient.size(); i++) {
            NucleoPatientDTO entry = nucleoPatient.get(i);
            requireField(entry, "payload.nucleoPatient[" + i + "]", correlationId);
            requireField(entry.nucleoPatientId(), "payload.nucleoPatient[" + i + "].nucleoPatientId", correlationId);
            requireField(entry.nucleoId(), "payload.nucleoPatient[" + i + "].nucleoId", correlationId);

            if (!uniqueIds.add(entry.nucleoPatientId())) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.INBOUND_DUPLICATE_ITEM,
                        correlationId,
                        "Item duplicado: payload.nucleoPatient.nucleoPatientId=" + entry.nucleoPatientId());
            }

            validateResponsaveis(entry.nucleoPatientResponsavel(), i, correlationId);
        }
    }

    private void validateResponsaveis(List<ResponsavelDTO> responsaveis, int nucleoIndex, String correlationId) {
        String fieldBase = "payload.nucleoPatient[" + nucleoIndex + "].nucleoPatientResponsavel";
        requireNotEmptyCollection(responsaveis, fieldBase, correlationId);

        for (int i = 0; i < responsaveis.size(); i++) {
            ResponsavelDTO responsavel = responsaveis.get(i);
            requireField(responsavel, fieldBase + "[" + i + "]", correlationId);
            requireField(responsavel.responsavelId(), fieldBase + "[" + i + "].responsavelId", correlationId);
            requireNotBlank(responsavel.role(), fieldBase + "[" + i + "].role", correlationId);
            validateRole(responsavel.role(), fieldBase + "[" + i + "].role", correlationId);
        }
    }

    private void validateRole(String role, String fieldName, String correlationId) {
        try {
            ResponsavelRole.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.INBOUND_INVALID_ENUM,
                    correlationId,
                    "Campo enum invalido: " + fieldName + "=" + role);
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

    private void requireNotBlank(String value, String fieldName, String correlationId) {
        if (value == null || value.isBlank()) {
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
