package com.humanizar.nucleorelacionamento.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.ResponsavelCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoCreatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoDeletedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.acolhimento.AcolhimentoUpdatedCommand;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.nucleo.NucleoPatientCommand;

@Component
public class AcolhimentoInboundMapper {

    public AcolhimentoCreatedCommand toCreatedCommand(AcolhimentoCreatedDTO acolhimentoCreatedDTO) {
        return new AcolhimentoCreatedCommand(
                acolhimentoCreatedDTO.patientId(),
                acolhimentoCreatedDTO.nucleoId(),
                toResponsavelCommands(acolhimentoCreatedDTO.nucleoPatientResponsavel()));
    }

    public AcolhimentoUpdatedCommand toUpdatedCommand(AcolhimentoUpdatedDTO acolhimentoUpdatedDTO) {
        List<NucleoPatientCommand> nucleos = acolhimentoUpdatedDTO.nucleoPatient() == null
                ? null
                : acolhimentoUpdatedDTO.nucleoPatient().stream()
                        .map(this::toNucleoPatientCommand)
                        .toList();

        return new AcolhimentoUpdatedCommand(acolhimentoUpdatedDTO.patientId(), nucleos);
    }

    public AcolhimentoDeletedCommand toDeletedCommand(AcolhimentoDeletedDTO acolhimentoDeletedDTO) {
        return new AcolhimentoDeletedCommand(acolhimentoDeletedDTO.patientId());
    }

    private NucleoPatientCommand toNucleoPatientCommand(NucleoPatientDTO nucleoPatientDTO) {
        return new NucleoPatientCommand(nucleoPatientDTO.nucleoId(),
                toResponsavelCommands(nucleoPatientDTO.nucleoPatientResponsavel()));
    }

    private List<ResponsavelCommand> toResponsavelCommands(List<ResponsavelDTO> responsaveis) {
        if (responsaveis == null) {
            return null;
        }
        return responsaveis.stream()
                .map(resp -> new ResponsavelCommand(resp.responsavelId(), resp.role()))
                .toList();
    }
}
