package com.humanizar.nucleorelacionamento.infrastructure.adapter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientResponsavelPort;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.NucleoPatientResponsavelEntity;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.repository.NucleoPatientResponsavelRepository;

@Component
public class NucleoPatientResponsavelAdapter implements NucleoPatientResponsavelPort {

    private final NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository;

    public NucleoPatientResponsavelAdapter(NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository) {
        this.nucleoPatientResponsavelRepository = nucleoPatientResponsavelRepository;
    }

    @Override
    public NucleoPatientResponsavel save(NucleoPatientResponsavel responsavel) {
        NucleoPatientResponsavelEntity entity = toEntity(responsavel);
        NucleoPatientResponsavelEntity saved = nucleoPatientResponsavelRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar nucleo patient responsavel"));
    }

    @Override
    public List<NucleoPatientResponsavel> saveAll(List<NucleoPatientResponsavel> responsaveis) {
        List<NucleoPatientResponsavelEntity> entities = responsaveis.stream()
                .map(this::toEntity)
                .toList();
        return nucleoPatientResponsavelRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<NucleoPatientResponsavel> findByNucleoPatientId(UUID nucleoPatientId) {
        return nucleoPatientResponsavelRepository.findByNucleoPatientId(nucleoPatientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByNucleoPatientId(UUID nucleoPatientId) {
        nucleoPatientResponsavelRepository.deleteByNucleoPatientId(nucleoPatientId);
    }

    private NucleoPatientResponsavel toDomain(NucleoPatientResponsavelEntity entity) {
        return new NucleoPatientResponsavel(
                entity.getId(),
                entity.getNucleoPatientId(),
                entity.getResponsavelId(),
                entity.getRole());
    }

    private NucleoPatientResponsavelEntity toEntity(NucleoPatientResponsavel domain) {
        UUID nucleoPatientId = Objects.requireNonNull(
                domain.getNucleoPatientId(),
                "nucleoPatientId é obrigatório para persistir NucleoPatientResponsavel");

        NucleoPatientResponsavelEntity entity = new NucleoPatientResponsavelEntity();
        entity.setId(domain.getId());
        entity.setNucleoPatientId(nucleoPatientId);
        entity.setResponsavelId(domain.getResponsavelId());
        entity.setRole(domain.getRole());
        return entity;
    }
}
