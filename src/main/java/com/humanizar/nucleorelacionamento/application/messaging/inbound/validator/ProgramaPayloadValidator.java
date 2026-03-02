package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class ProgramaPayloadValidator {

    public void validateCreated(List<ProgramaDTO> payload, String correlationId) {
        validateAbordagemList(payload, correlationId);
    }

    public void validateUpdated(List<ProgramaDTO> payload, String correlationId) {
        validateAbordagemList(payload, correlationId);
    }

    public void validateDeleted(ProgramaDeletedDTO command, String correlationId) {
        requireNotNull(command, "payload programa.deleted e obrigatorio", correlationId);
        requireNotNull(command.patientId(), "patientId e obrigatorio", correlationId);
    }

    private void validateAbordagemList(List<ProgramaDTO> nucleoAbordagens, String correlationId) {
        requireNotEmpty(nucleoAbordagens, "nucleoAbordagens e obrigatorio", correlationId);

        for (int i = 0; i < nucleoAbordagens.size(); i++) {
            ProgramaDTO nucleoAbordagem = nucleoAbordagens.get(i);
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
