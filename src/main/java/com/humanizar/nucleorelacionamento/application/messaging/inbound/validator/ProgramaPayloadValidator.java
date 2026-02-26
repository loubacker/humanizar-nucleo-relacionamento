package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoAbordagemCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaUpdatedCommand;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramaPayloadValidator {

    public void validateCreated(ProgramaCreatedCommand command, String correlationId) {
        validateAbordagemList(command.nucleoAbordagens(), correlationId);
    }

    public void validateUpdated(ProgramaUpdatedCommand command, String correlationId) {
        validateAbordagemList(command.nucleoAbordagens(), correlationId);
    }

    public void validateDeleted(ProgramaDeletedCommand command, String correlationId) {
        if (command.patientId() == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "patientId e obrigatorio");
        }
    }

    private void validateAbordagemList(List<NucleoAbordagemCommand> nucleoAbordagens, String correlationId) {
        if (nucleoAbordagens == null || nucleoAbordagens.isEmpty()) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId, "nucleoAbordagens e obrigatorio");
        }
        for (NucleoAbordagemCommand nucleoAbordagem : nucleoAbordagens) {
            if (nucleoAbordagem.nucleoPatientId() == null) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "nucleoPatientId e obrigatorio");
            }
            if (nucleoAbordagem.abordagemId() == null || nucleoAbordagem.abordagemId().isEmpty()) {
                throw new NucleoRelacionamentoException(
                        ReasonCode.VALIDATION_ERROR, correlationId, "abordagemId e obrigatorio");
            }
        }
    }
}
