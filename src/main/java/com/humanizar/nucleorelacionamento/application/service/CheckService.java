package com.humanizar.nucleorelacionamento.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.nucleorelacionamento.application.dto.BlockedNucleoDTO;
import com.humanizar.nucleorelacionamento.application.dto.CheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.mapper.CheckMapper;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;

@Service
public class CheckService {

    private final NucleoPatientPort nucleoPatientPort;
    private final AbordagemPatientPort abordagemPatientPort;
    private final CheckMapper checkMapper;

    public CheckService(
            NucleoPatientPort nucleoPatientPort,
            AbordagemPatientPort abordagemPatientPort,
            CheckMapper checkMapper) {
        this.nucleoPatientPort = nucleoPatientPort;
        this.abordagemPatientPort = abordagemPatientPort;
        this.checkMapper = checkMapper;
    }

    public CheckResponseDTO checkDeleteStatusByPatientId(UUID patientId) {
        if (patientId == null) {
            throw new NucleoRelacionamentoException(
                    ReasonCode.VALIDATION_ERROR,
                    null,
                    "patientId é obrigatório");
        }

        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        if (nucleos.isEmpty()) {
            return checkMapper.toAllowed();
        }

        List<BlockedNucleoDTO> blocked = new ArrayList<>();
        for (NucleoPatient nucleo : nucleos) {
            int abordagemCount = abordagemPatientPort.findByNucleoPatientId(nucleo.getId()).size();
            if (abordagemCount > 0) {
                blocked.add(new BlockedNucleoDTO(nucleo.getNucleoId(), abordagemCount));
            }
        }

        if (blocked.isEmpty()) {
            return checkMapper.toAllowed();
        }
        return checkMapper.toBlocked(blocked);
    }
}
