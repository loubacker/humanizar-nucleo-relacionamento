package com.humanizar.nucleorelacionamento.infrastructure.persistence.entity;

import java.util.Objects;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nucleo_patient_responsavel")
public class NucleoPatientResponsavelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nucleo_patient_id", nullable = false)
    private UUID nucleoPatientId;

    @Column(name = "responsavel_id", nullable = false)
    private UUID responsavelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ResponsavelRole role;

    // Construtores
    public NucleoPatientResponsavelEntity() {
    }

    public NucleoPatientResponsavelEntity(UUID id, UUID nucleoPatientId, UUID responsavelId,
            ResponsavelRole role) {
        this.id = id;
        this.nucleoPatientId = nucleoPatientId;
        this.responsavelId = responsavelId;
        this.role = role;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNucleoPatientId() {
        return nucleoPatientId;
    }

    public void setNucleoPatientId(UUID nucleoPatientId) {
        this.nucleoPatientId = nucleoPatientId;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(UUID responsavelId) {
        this.responsavelId = responsavelId;
    }

    public ResponsavelRole getRole() {
        return role;
    }

    public void setRole(ResponsavelRole role) {
        this.role = role;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NucleoPatientResponsavelEntity that = (NucleoPatientResponsavelEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(nucleoPatientId, that.nucleoPatientId)
                && Objects.equals(responsavelId, that.responsavelId)
                && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nucleoPatientId, responsavelId, role);
    }

    @Override
    public String toString() {
        return "NucleoPatientResponsavelEntity{" +
                "id=" + id +
                ", nucleoPatientId=" + nucleoPatientId +
                ", responsavelId=" + responsavelId +
                ", role=" + role +
                '}';
    }
}
