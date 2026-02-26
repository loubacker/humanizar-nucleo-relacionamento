package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoUpdatedCommand;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

import org.springframework.stereotype.Component;

@Component
public class AcolhimentoPayloadValidator {

    public void validateCreated(AcolhimentoCreatedCommand command, String correlationId) {
        if (command.patientId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "patientId e obrigatorio");
        }
        if (command.nucleoId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "nucleoId e obrigatorio");
        }
        if (command.nucleoPatientResponsavel() == null || command.nucleoPatientResponsavel().isEmpty()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "nucleoPatientResponsavel e obrigatorio");
        }
        command.nucleoPatientResponsavel().forEach(resp -> {
            if (resp.responsavelId() == null) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "responsavelId e obrigatorio");
            }
            if (resp.role() == null || resp.role().isBlank()) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "role e obrigatorio");
            }
        });
    }

    public void validateUpdated(AcolhimentoUpdatedCommand command, String correlationId) {
        if (command.patientId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "patientId e obrigatorio");
        }
        if (command.nucleoPatient() == null || command.nucleoPatient().isEmpty()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "nucleoPatient e obrigatorio");
        }
        command.nucleoPatient().forEach(nucleo -> {
            if (nucleo.nucleoId() == null) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "nucleoId e obrigatorio");
            }
            if (nucleo.nucleoPatientResponsavel() == null || nucleo.nucleoPatientResponsavel().isEmpty()) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "nucleoPatientResponsavel e obrigatorio");
            }
            nucleo.nucleoPatientResponsavel().forEach(resp -> {
                if (resp.responsavelId() == null) {
                    throw new NucleoRelacionamentoException(
                            ReasonCode.VALIDATION_ERROR, correlationId, "responsavelId e obrigatorio");
                }
                if (resp.role() == null || resp.role().isBlank()) {
                    throw new NucleoRelacionamentoException(
                            ReasonCode.VALIDATION_ERROR, correlationId, "role e obrigatorio");
                }
            });
        });
    }

    public void validateDeleted(AcolhimentoDeletedCommand command, String correlationId) {
        if (command.patientId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "patientId e obrigatorio");
        }
    }
}
