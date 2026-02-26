package com.humanizar.nucleorelacionamento.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaItemDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoAbordagemCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.programa.ProgramaUpdatedCommand;

@Component
public class ProgramaInboundMapper {

    public ProgramaCreatedCommand toCreatedCommand(List<ProgramaItemDTO> programaItems) {
        return new ProgramaCreatedCommand(toNucleoAbordagemCommands(programaItems));
    }

    public ProgramaUpdatedCommand toUpdatedCommand(List<ProgramaItemDTO> programaItems) {
        return new ProgramaUpdatedCommand(toNucleoAbordagemCommands(programaItems));
    }

    public ProgramaDeletedCommand toDeletedCommand(ProgramaDeletedDTO programaDeletedDTO) {
        return new ProgramaDeletedCommand(programaDeletedDTO.patientId());
    }

    private List<NucleoAbordagemCommand> toNucleoAbordagemCommands(List<ProgramaItemDTO> programaItems) {
        if (programaItems == null) {
            return null;
        }
        return programaItems.stream()
                .map(programaItem -> new NucleoAbordagemCommand(programaItem.nucleoPatientId(),
                        programaItem.abordagemId()))
                .toList();
    }
}
