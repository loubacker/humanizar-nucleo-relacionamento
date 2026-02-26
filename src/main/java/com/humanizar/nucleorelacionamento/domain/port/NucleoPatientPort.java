package com.humanizar.nucleorelacionamento.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;

public interface NucleoPatientPort {

    NucleoPatient save(NucleoPatient nucleoPatient);

    boolean existsById(UUID id);

    List<NucleoPatient> findAllByPatientId(UUID patientId);

    Optional<NucleoPatient> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);

    void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);
}
