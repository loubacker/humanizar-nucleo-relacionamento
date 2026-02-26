package com.humanizar.nucleorelacionamento.infrastructure.persistence.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "abordagem_patient", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "abordagem_id", "nucleo_patient_id" })
})
public class AbordagemPatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nucleo_patient_id", nullable = false)
    private UUID nucleoPatientId;

    @Column(name = "abordagem_id", nullable = false)
    private UUID abordagemId;

    // Construtores
    public AbordagemPatientEntity() {
    }

    public AbordagemPatientEntity(UUID id, UUID nucleoPatientId, UUID abordagemId) {
        this.id = id;
        this.nucleoPatientId = nucleoPatientId;
        this.abordagemId = abordagemId;
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

    public UUID getAbordagemId() {
        return abordagemId;
    }

    public void setAbordagemId(UUID abordagemId) {
        this.abordagemId = abordagemId;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbordagemPatientEntity that = (AbordagemPatientEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(nucleoPatientId, that.nucleoPatientId)
                && Objects.equals(abordagemId, that.abordagemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nucleoPatientId, abordagemId);
    }

    @Override
    public String toString() {
        return "AbordagemPatientEntity{" +
                "id=" + id +
                ", nucleoPatientId=" + nucleoPatientId +
                ", abordagemId=" + abordagemId +
                '}';
    }
}
