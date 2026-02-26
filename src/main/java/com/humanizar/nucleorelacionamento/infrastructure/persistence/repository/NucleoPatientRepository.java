package com.humanizar.nucleorelacionamento.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.NucleoPatientEntity;

@Repository
public interface NucleoPatientRepository extends JpaRepository<NucleoPatientEntity, UUID> {

    List<NucleoPatientEntity> findAllByPatientId(UUID patientId);

    Optional<NucleoPatientEntity> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);

    void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);
}
