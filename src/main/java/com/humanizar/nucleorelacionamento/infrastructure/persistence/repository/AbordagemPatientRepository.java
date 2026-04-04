package com.humanizar.nucleorelacionamento.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.AbordagemPatientEntity;

@Repository
public interface AbordagemPatientRepository extends JpaRepository<AbordagemPatientEntity, UUID> {

    List<AbordagemPatientEntity> findByNucleoPatientId(UUID nucleoPatientId);

    List<AbordagemPatientEntity> findByNucleoPatientIdIn(List<UUID> nucleoPatientIds);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
