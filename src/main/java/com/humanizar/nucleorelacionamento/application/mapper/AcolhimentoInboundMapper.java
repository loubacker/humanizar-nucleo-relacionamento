package com.humanizar.nucleorelacionamento.application.mapper;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;

@Component
public class AcolhimentoInboundMapper {

    public AcolhimentoCreatedDTO toCreatedPayload(AcolhimentoCreatedDTO acolhimentoCreatedDTO) {
        return acolhimentoCreatedDTO;
    }

    public AcolhimentoUpdatedDTO toUpdatedPayload(AcolhimentoUpdatedDTO acolhimentoUpdatedDTO) {
        return acolhimentoUpdatedDTO;
    }

    public AcolhimentoDeletedDTO toDeletedPayload(AcolhimentoDeletedDTO acolhimentoDeletedDTO) {
        return acolhimentoDeletedDTO;
    }
}
