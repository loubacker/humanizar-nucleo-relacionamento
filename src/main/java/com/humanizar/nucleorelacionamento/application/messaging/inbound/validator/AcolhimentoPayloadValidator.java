package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.ResponsavelCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoUpdatedCommand;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class AcolhimentoPayloadValidator {

    public void validateCreated(AcolhimentoCreatedCommand command, String correlationId) {
        requireNotNull(command, "payload acolhimento.created e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
        requireNotNull(command.nucleoId(), "nucleoId e obrigatorio", correlationId);
        validateResponsaveis(command.nucleoPatientResponsavel(), "nucleoPatientResponsavel", correlationId);
    }

    public void validateUpdated(AcolhimentoUpdatedCommand command, String correlationId) {
        requireNotNull(command, "payload acolhimento.updated e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
        requireNotEmpty(command.nucleoPatient(), "nucleoPatient e obrigatorio", correlationId);

        for (int i = 0; i < command.nucleoPatient().size(); i++) {
            var nucleo = command.nucleoPatient().get(i);
            requireNotNull(nucleo, "nucleoPatient[" + i + "] e obrigatorio", correlationId);
            requireNotNull(nucleo.nucleoId(), "nucleoPatient[" + i + "].nucleoId e obrigatorio", correlationId);
            validateResponsaveis(nucleo.nucleoPatientResponsavel(),
                    "nucleoPatient[" + i + "].nucleoPatientResponsavel",
                    correlationId);
        }
    }

    public void validateDeleted(AcolhimentoDeletedCommand command, String correlationId) {
        requireNotNull(command, "payload acolhimento.deleted e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
    }

    private void validateResponsaveis(List<ResponsavelCommand> responsaveis,
            String fieldPath,
            String correlationId) {
        requireNotEmpty(responsaveis, fieldPath + " e obrigatorio", correlationId);
        for (int i = 0; i < responsaveis.size(); i++) {
            ResponsavelCommand responsavel = responsaveis.get(i);
            requireNotNull(responsavel, fieldPath + "[" + i + "] e obrigatorio", correlationId);
            requireNotNull(responsavel.responsavelId(),
                    fieldPath + "[" + i + "].responsavelId e obrigatorio", correlationId);
            requireNotBlank(responsavel.role(),
                    fieldPath + "[" + i + "].role e obrigatorio", correlationId);
        }
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
