package com.humanizar.nucleorelacionamento.domain.port;

import java.util.List;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;

public interface AbordagemPatientPort {

    AbordagemPatient save(AbordagemPatient abordagemPatient);

    List<AbordagemPatient> saveAll(List<AbordagemPatient> abordagens);

    List<AbordagemPatient> findByNucleoPatientId(UUID nucleoPatientId);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
