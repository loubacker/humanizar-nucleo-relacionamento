package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class AcolhimentoPayloadValidator {

    public void validateCreated(AcolhimentoCreatedDTO command, String correlationId) {
        requireNotNull(command, "payload acolhimento.created e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
        validateNucleoPatients(command.nucleoPatient(), correlationId);
    }

    public void validateUpdated(AcolhimentoUpdatedDTO command, String correlationId) {
        requireNotNull(command, "payload acolhimento.updated e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
        validateNucleoPatients(command.nucleoPatient(), correlationId);
    }

    public void validateDeleted(AcolhimentoDeletedDTO command, String correlationId) {
        requireNotNull(command, "payload acolhimento.deleted e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
    }

    private void validateResponsaveis(List<ResponsavelDTO> responsaveis,
            String fieldPath,
            String correlationId) {
        requireNotEmpty(responsaveis, fieldPath + " e obrigatorio", correlationId);
        for (int i = 0; i < responsaveis.size(); i++) {
            ResponsavelDTO responsavel = responsaveis.get(i);
            requireNotNull(responsavel, fieldPath + "[" + i + "] e obrigatorio", correlationId);
            requireNotNull(responsavel.responsavelId(),
                    fieldPath + "[" + i + "].responsavelId e obrigatorio", correlationId);
            requireNotBlank(responsavel.role(),
                    fieldPath + "[" + i + "].role e obrigatorio", correlationId);
        }
    }

    private void validateNucleoPatients(List<NucleoPatientDTO> nucleoPatients,
            String correlationId) {
        requireNotEmpty(nucleoPatients, "nucleoPatient e obrigatorio", correlationId);
        for (int i = 0; i < nucleoPatients.size(); i++) {
            var nucleo = nucleoPatients.get(i);
            requireNotNull(nucleo, "nucleoPatient[" + i + "] e obrigatorio", correlationId);
            requireNotNull(nucleo.nucleoPatientId(),
                    "nucleoPatient[" + i + "].nucleoPatientId e obrigatorio", correlationId);
            requireNotNull(nucleo.nucleoId(), "nucleoPatient[" + i + "].nucleoId e obrigatorio", correlationId);
            validateResponsaveis(nucleo.nucleoPatientResponsavel(),
                    "nucleoPatient[" + i + "].nucleoPatientResponsavel",
                    correlationId);
        }

        Set<UUID> uniqueNucleoPatientIds = nucleoPatients.stream()
                .map(NucleoPatientDTO::nucleoPatientId)
                .collect(Collectors.toSet());
        requireTrue(uniqueNucleoPatientIds.size() == nucleoPatients.size(),
                "nucleoPatientId duplicado no payload", correlationId);
    }

    private void requireNotNull(Object value, String message, String correlationId) {
        requireTrue(value != null, message, correlationId);
    }

    private void requireNotBlank(String value, String message, String correlationId) {
        requireTrue(value != null && !value.isBlank(), message, correlationId);
    }

    private void requireNotEmpty(List<?> value, String message, String correlationId) {
        requireTrue(value != null && !value.isEmpty(), message, correlationId);
    }

    private void requireTrue(boolean condition, String message, String correlationId) {
        if (!condition) {
            throw new NucleoRelacionamentoException(ReasonCode.VALIDATION_ERROR, correlationId, message);
        }
    }
}
