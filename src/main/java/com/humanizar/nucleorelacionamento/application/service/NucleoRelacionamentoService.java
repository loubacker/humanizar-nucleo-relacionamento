package com.humanizar.nucleorelacionamento.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.mapper.NucleoRelacionamentoMapper;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientResponsavelPort;

@Service
public class NucleoRelacionamentoService {

    private final NucleoPatientPort nucleoPatientPort;
    private final NucleoPatientResponsavelPort nucleoPatientResponsavelPort;
    private final AbordagemPatientPort abordagemPatientPort;
    private final NucleoRelacionamentoMapper nucleoRelacionamentoMapper;

    public NucleoRelacionamentoService(
            NucleoPatientPort nucleoPatientPort,
            NucleoPatientResponsavelPort nucleoPatientResponsavelPort,
            AbordagemPatientPort abordagemPatientPort,
            NucleoRelacionamentoMapper nucleoRelacionamentoMapper) {
        this.nucleoPatientPort = nucleoPatientPort;
        this.nucleoPatientResponsavelPort = nucleoPatientResponsavelPort;
        this.abordagemPatientPort = abordagemPatientPort;
        this.nucleoRelacionamentoMapper = nucleoRelacionamentoMapper;
    }

    public NucleoRelacionamentoRetrieveDTO findByPatientId(UUID patientId) {
        if (patientId == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    null,
                    "patientId é obrigatório");
        }

        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        if (nucleos.isEmpty()) {
            throw new NucleoRelacionamentoException(ReasonCode.PATIENT_NOT_FOUND, null);
        }

        List<UUID> nucleoPatientIds = nucleos.stream()
                .map(NucleoPatient::getId)
                .toList();

        Map<UUID, List<NucleoPatientResponsavel>> responsaveisByNucleoPatientId = nucleoPatientResponsavelPort
                .findAllResponsaveisByNucleoPatientId(nucleoPatientIds)
                .stream()
                .collect(Collectors.groupingBy(NucleoPatientResponsavel::getNucleoPatientId));

        Map<UUID, List<AbordagemPatient>> abordagensByNucleoPatientId = abordagemPatientPort
                .findAllAbordagensByNucleoPatientId(nucleoPatientIds)
                .stream()
                .collect(Collectors.groupingBy(AbordagemPatient::getNucleoPatientId));

        return nucleoRelacionamentoMapper.toRetrieve(
                patientId,
                nucleos,
                responsaveisByNucleoPatientId,
                abordagensByNucleoPatientId);
    }
}
