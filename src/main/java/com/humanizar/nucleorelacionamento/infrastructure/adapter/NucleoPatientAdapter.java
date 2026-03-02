package com.humanizar.nucleorelacionamento.infrastructure.adapter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.entity.NucleoPatientEntity;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.repository.AbordagemPatientRepository;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.repository.NucleoPatientRepository;
import com.humanizar.nucleorelacionamento.infrastructure.persistence.repository.NucleoPatientResponsavelRepository;

@Component
public class NucleoPatientAdapter implements NucleoPatientPort {

    private final NucleoPatientRepository nucleoPatientRepository;
    private final AbordagemPatientRepository abordagemPatientRepository;
    private final NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository;

    public NucleoPatientAdapter(NucleoPatientRepository nucleoPatientRepository,
            AbordagemPatientRepository abordagemPatientRepository,
            NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository) {
        this.nucleoPatientRepository = nucleoPatientRepository;
        this.abordagemPatientRepository = abordagemPatientRepository;
        this.nucleoPatientResponsavelRepository = nucleoPatientResponsavelRepository;
    }

    @Override
    public boolean existsById(UUID id) {
        return nucleoPatientRepository.existsById(id);
    }

    @Override
    public NucleoPatient save(NucleoPatient nucleoPatient) {
        NucleoPatientEntity entity = toEntity(nucleoPatient);
        NucleoPatientEntity saved = nucleoPatientRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar nucleo patient"));
    }

    @Override
    public List<NucleoPatient> findAllByPatientId(UUID patientId) {
        return nucleoPatientRepository.findAllByPatientId(patientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NucleoPatient> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId) {
        return nucleoPatientRepository.findByPatientIdAndNucleoId(patientId, nucleoId)
                .map(this::toDomain);
    }

    @Override
    public void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId) {
        nucleoPatientRepository.findByPatientIdAndNucleoId(patientId, nucleoId)
                .ifPresent(entity -> {
                    if (!abordagemPatientRepository.findByNucleoPatientId(entity.getId()).isEmpty()) {
                        throw new NucleoRelacionamentoException(ReasonCode.HAS_ABORDAGEM, null);
                    }
                    nucleoPatientResponsavelRepository.deleteByNucleoPatientId(entity.getId());
                    nucleoPatientRepository.deleteByPatientIdAndNucleoId(patientId, nucleoId);
                });
    }

    private NucleoPatient toDomain(NucleoPatientEntity entity) {
        return new NucleoPatient(
                entity.getId(),
                entity.getPatientId(),
                entity.getNucleoId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null,
                null);
    }

    private NucleoPatientEntity toEntity(NucleoPatient domain) {
        UUID id = Objects.requireNonNull(
                domain.getId(),
                "id (nucleoPatientId) e obrigatorio para persistir NucleoPatient");

        NucleoPatientEntity entity = new NucleoPatientEntity();
        entity.setId(id);
        entity.setPatientId(domain.getPatientId());
        entity.setNucleoId(domain.getNucleoId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
