package com.humanizar.nucleorelacionamento.domain.port;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;

public interface NucleoPatientResponsavelPort {

    NucleoPatientResponsavel save(NucleoPatientResponsavel responsavel);

    List<NucleoPatientResponsavel> saveAll(List<NucleoPatientResponsavel> responsaveis);

    List<NucleoPatientResponsavel> findByNucleoPatientId(UUID nucleoPatientId);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
