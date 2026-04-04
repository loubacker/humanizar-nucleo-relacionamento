package com.humanizar.nucleorelacionamento.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.NucleoPatientResponsavelEntity;

@Repository
public interface NucleoPatientResponsavelRepository extends JpaRepository<NucleoPatientResponsavelEntity, UUID> {

    List<NucleoPatientResponsavelEntity> findByNucleoPatientId(UUID nucleoPatientId);

    List<NucleoPatientResponsavelEntity> findByNucleoPatientIdIn(List<UUID> nucleoPatientIds);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
