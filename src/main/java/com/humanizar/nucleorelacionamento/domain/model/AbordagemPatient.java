package com.humanizar.nucleorelacionamento.domain.model;

import java.util.Objects;
import java.util.UUID;

public class AbordagemPatient {

    private UUID id;
    private UUID nucleoPatientId;
    private UUID abordagemId;

    public AbordagemPatient() {
    }

    public AbordagemPatient(UUID id, UUID nucleoPatientId, UUID abordagemId) {
        this.id = id;
        this.nucleoPatientId = nucleoPatientId;
        this.abordagemId = abordagemId;
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID nucleoPatientId;
        private UUID abordagemId;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder nucleoPatientId(UUID nucleoPatientId) {
            this.nucleoPatientId = nucleoPatientId;
            return this;
        }

        public Builder abordagemId(UUID abordagemId) {
            this.abordagemId = abordagemId;
            return this;
        }

        public AbordagemPatient build() {
            return new AbordagemPatient(id, nucleoPatientId, abordagemId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbordagemPatient that = (AbordagemPatient) o;
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
        return "AbordagemPatient{" +
                "id=" + id +
                ", nucleoPatientId=" + nucleoPatientId +
                ", abordagemId=" + abordagemId +
                '}';
    }
}
