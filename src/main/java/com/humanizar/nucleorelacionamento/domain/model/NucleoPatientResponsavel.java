package com.humanizar.nucleorelacionamento.domain.model;

import java.util.Objects;
import java.util.UUID;

import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;

public class NucleoPatientResponsavel {

    private UUID id;
    private UUID nucleoPatientId;
    private UUID responsavelId;
    private ResponsavelRole role;

    public NucleoPatientResponsavel() {
    }

    public NucleoPatientResponsavel(UUID id, UUID responsavelId, ResponsavelRole role) {
        this(id, null, responsavelId, role);
    }

    public NucleoPatientResponsavel(UUID id, UUID nucleoPatientId, UUID responsavelId, ResponsavelRole role) {
        this.id = id;
        this.nucleoPatientId = nucleoPatientId;
        this.responsavelId = responsavelId;
        this.role = role;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID nucleoPatientId;
        private UUID responsavelId;
        private ResponsavelRole role;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder nucleoPatientId(UUID nucleoPatientId) {
            this.nucleoPatientId = nucleoPatientId;
            return this;
        }

        public Builder responsavelId(UUID responsavelId) {
            this.responsavelId = responsavelId;
            return this;
        }

        public Builder role(ResponsavelRole role) {
            this.role = role;
            return this;
        }

        public NucleoPatientResponsavel build() {
            return new NucleoPatientResponsavel(id, nucleoPatientId, responsavelId, role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NucleoPatientResponsavel that = (NucleoPatientResponsavel) o;
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
        return "NucleoPatientResponsavel{" +
                "id=" + id +
                ", nucleoPatientId=" + nucleoPatientId +
                ", responsavelId=" + responsavelId +
                ", role=" + role +
                '}';
    }
}
