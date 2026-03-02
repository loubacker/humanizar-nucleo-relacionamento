package com.humanizar.nucleorelacionamento.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;

@Component
public class ProgramaInboundMapper {

    public List<ProgramaDTO> toCreatedPayload(List<ProgramaDTO> programaItems) {
        return programaItems;
    }

    public List<ProgramaDTO> toUpdatedPayload(List<ProgramaDTO> programaItems) {
        return programaItems;
    }

    public ProgramaDeletedDTO toDeletedPayload(ProgramaDeletedDTO programaDeletedDTO) {
        return programaDeletedDTO;
    }
}
