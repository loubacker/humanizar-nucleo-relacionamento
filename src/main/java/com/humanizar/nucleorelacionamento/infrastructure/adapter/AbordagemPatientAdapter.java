package com.humanizar.nucleorelacionamento.infrastructure.adapter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.AbordagemPatientEntity;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.repository.AbordagemPatientRepository;

@Component
public class AbordagemPatientAdapter implements AbordagemPatientPort {

    private final AbordagemPatientRepository abordagemPatientRepository;

    public AbordagemPatientAdapter(AbordagemPatientRepository abordagemPatientRepository) {
        this.abordagemPatientRepository = abordagemPatientRepository;
    }

    @Override
    public AbordagemPatient save(AbordagemPatient abordagemPatient) {
        AbordagemPatientEntity entity = toEntity(abordagemPatient);
        AbordagemPatientEntity saved = abordagemPatientRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar abordagem patient"));
    }

    @Override
    public List<AbordagemPatient> saveAll(List<AbordagemPatient> abordagens) {
        List<AbordagemPatientEntity> entities = abordagens.stream()
                .map(this::toEntity)
                .toList();
        return abordagemPatientRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AbordagemPatient> findByNucleoPatientId(UUID nucleoPatientId) {
        return abordagemPatientRepository.findByNucleoPatientId(nucleoPatientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByNucleoPatientId(UUID nucleoPatientId) {
        abordagemPatientRepository.deleteByNucleoPatientId(nucleoPatientId);
    }

    private AbordagemPatient toDomain(AbordagemPatientEntity entity) {
        return new AbordagemPatient(
                entity.getId(),
                entity.getNucleoPatientId(),
                entity.getAbordagemId());
    }

    private AbordagemPatientEntity toEntity(AbordagemPatient domain) {
        AbordagemPatientEntity entity = new AbordagemPatientEntity();
        entity.setId(domain.getId());
        entity.setNucleoPatientId(domain.getNucleoPatientId());
        entity.setAbordagemId(domain.getAbordagemId());
        return entity;
    }
}
