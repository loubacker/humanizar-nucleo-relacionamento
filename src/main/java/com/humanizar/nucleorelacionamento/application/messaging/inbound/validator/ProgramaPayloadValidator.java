package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoAbordagemCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaUpdatedCommand;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class ProgramaPayloadValidator {

    public void validateCreated(ProgramaCreatedCommand command, String correlationId) {
        requireNotNull(command, "payload programa.created e obrigatorio", correlationId);
        validateAbordagemList(command.nucleoAbordagens(), correlationId);
    }

    public void validateUpdated(ProgramaUpdatedCommand command, String correlationId) {
        requireNotNull(command, "payload programa.updated e obrigatorio", correlationId);
        validateAbordagemList(command.nucleoAbordagens(), correlationId);
    }

    public void validateDeleted(ProgramaDeletedCommand command, String correlationId) {
        requireNotNull(command, "payload programa.deleted e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
    }

    private void validateAbordagemList(List<NucleoAbordagemCommand> nucleoAbordagens, String correlationId) {
        requireNotEmpty(nucleoAbordagens, "nucleoAbordagens e obrigatorio", correlationId);

        for (int i = 0; i < nucleoAbordagens.size(); i++) {
            NucleoAbordagemCommand nucleoAbordagem = nucleoAbordagens.get(i);
            requireNotNull(nucleoAbordagem, "nucleoAbordagens[" + i + "] e obrigatorio", correlationId);
            requireNotNull(nucleoAbordagem.nucleoPatientId(),
                    "nucleoAbordagens[" + i + "].nucleoPatientId e obrigatorio",
                    correlationId);
            requireNotEmpty(nucleoAbordagem.abordagemId(),
                    "nucleoAbordagens[" + i + "].abordagemId e obrigatorio",
                    correlationId);

            for (int j = 0; j < nucleoAbordagem.abordagemId().size(); j++) {
                requireNotNull(nucleoAbordagem.abordagemId().get(j),
                        "nucleoAbordagens[" + i + "].abordagemId[" + j + "] e obrigatorio",
                        correlationId);
            }
        }
    }

    private void requireNotNull(Object value, String message, String correlationId) {
        requireTrue(value != null, message, correlationId);
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
