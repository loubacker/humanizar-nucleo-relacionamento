package com.humanizar.nucleorelacionamento.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NucleoPatient {

    private UUID id;
    private UUID patientId;
    private UUID nucleoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NucleoPatientResponsavel> responsaveis;
    private List<AbordagemPatient> abordagens;

    public NucleoPatient() {
        this.responsaveis = new ArrayList<>();
        this.abordagens = new ArrayList<>();
    }

    public NucleoPatient(UUID id, UUID patientId, UUID nucleoId, LocalDateTime createdAt,
            LocalDateTime updatedAt, List<NucleoPatientResponsavel> responsaveis,
            List<AbordagemPatient> abordagens) {
        this.id = id;
        this.patientId = patientId;
        this.nucleoId = nucleoId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.responsaveis = responsaveis != null ? responsaveis : new ArrayList<>();
        this.abordagens = abordagens != null ? abordagens : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getNucleoId() {
        return nucleoId;
    }

    public void setNucleoId(UUID nucleoId) {
        this.nucleoId = nucleoId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<NucleoPatientResponsavel> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<NucleoPatientResponsavel> responsaveis) {
        this.responsaveis = responsaveis;
    }

    public List<AbordagemPatient> getAbordagens() {
        return abordagens;
    }

    public void setAbordagens(List<AbordagemPatient> abordagens) {
        this.abordagens = abordagens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID patientId;
        private UUID nucleoId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<NucleoPatientResponsavel> responsaveis;
        private List<AbordagemPatient> abordagens;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder nucleoId(UUID nucleoId) {
            this.nucleoId = nucleoId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder responsaveis(List<NucleoPatientResponsavel> responsaveis) {
            this.responsaveis = responsaveis;
            return this;
        }

        public Builder abordagens(List<AbordagemPatient> abordagens) {
            this.abordagens = abordagens;
            return this;
        }

        public NucleoPatient build() {
            return new NucleoPatient(id, patientId, nucleoId, createdAt, updatedAt, responsaveis, abordagens);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NucleoPatient that = (NucleoPatient) o;
        return Objects.equals(id, that.id)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(nucleoId, that.nucleoId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(responsaveis, that.responsaveis)
                && Objects.equals(abordagens, that.abordagens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, nucleoId, createdAt, updatedAt, responsaveis, abordagens);
    }

    @Override
    public String toString() {
        return "NucleoPatient{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", nucleoId=" + nucleoId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", responsaveis=" + responsaveis +
                ", abordagens=" + abordagens +
                '}';
    }
}
