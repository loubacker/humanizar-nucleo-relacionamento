package com.humanizar.nucleorelacionamento.application.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoDTO;
import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;

@Component
public class NucleoRelacionamentoMapper {

    public NucleoRelacionamentoRetrieveDTO toRetrieve(
            UUID patientId,
            List<NucleoPatient> nucleos,
            Map<UUID, List<NucleoPatientResponsavel>> responsaveisByNucleoPatientId,
            Map<UUID, List<AbordagemPatient>> abordagensByNucleoPatientId) {
        List<NucleoRelacionamentoDTO> data = nucleos.stream()
                .map(nucleo -> toItem(
                        nucleo,
                        responsaveisByNucleoPatientId.getOrDefault(nucleo.getId(), List.of()),
                        abordagensByNucleoPatientId.getOrDefault(nucleo.getId(), List.of())))
                .toList();

        return new NucleoRelacionamentoRetrieveDTO(patientId, data);
    }

    private NucleoRelacionamentoDTO toItem(
            NucleoPatient nucleo,
            List<NucleoPatientResponsavel> responsaveis,
            List<AbordagemPatient> abordagens) {
        List<ResponsavelDTO> responsavelDTOs = responsaveis.stream()
                .map(responsavel -> new ResponsavelDTO(
                        responsavel.getResponsavelId(),
                        responsavel.getRole().name()))
                .toList();

        List<UUID> abordagemIds = abordagens.stream()
                .map(AbordagemPatient::getAbordagemId)
                .toList();

        return new NucleoRelacionamentoDTO(
                nucleo.getId(),
                nucleo.getNucleoId(),
                responsavelDTOs,
                abordagemIds);
    }
}
