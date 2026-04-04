package com.humanizar.nucleorelacionamento.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;

class NucleoRelacionamentoMapperTest {

    private final NucleoRelacionamentoMapper mapper = new NucleoRelacionamentoMapper();

    @Test
    void shouldMapRetrievePayloadWithResponsaveisAndAbordagens() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID abordagemId = UUID.randomUUID();

        NucleoPatient nucleo = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();

        NucleoPatientResponsavel responsavel = new NucleoPatientResponsavel(
                UUID.randomUUID(),
                nucleoPatientId,
                responsavelId,
                ResponsavelRole.COORDENADOR);

        AbordagemPatient abordagem = new AbordagemPatient(
                UUID.randomUUID(),
                nucleoPatientId,
                abordagemId);

        NucleoRelacionamentoRetrieveDTO response = mapper.toRetrieve(
                patientId,
                List.of(nucleo),
                Map.of(nucleoPatientId, List.of(responsavel)),
                Map.of(nucleoPatientId, List.of(abordagem)));

        assertEquals(patientId, response.patientId());
        assertEquals(1, response.nucleoPatient().size());
        assertEquals(nucleoPatientId, response.nucleoPatient().get(0).nucleoPatientId());
        assertEquals(nucleoId, response.nucleoPatient().get(0).nucleoId());
        assertEquals(1, response.nucleoPatient().get(0).nucleoPatientResponsavel().size());
        assertEquals(responsavelId, response.nucleoPatient().get(0).nucleoPatientResponsavel().get(0).responsavelId());
        assertEquals("COORDENADOR", response.nucleoPatient().get(0).nucleoPatientResponsavel().get(0).role());
        assertEquals(List.of(abordagemId), response.nucleoPatient().get(0).abordagemId());
    }

    @Test
    void shouldMapEmptyCollectionsWhenNucleoHasNoResponsaveisOrAbordagens() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        NucleoPatient nucleo = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();

        NucleoRelacionamentoRetrieveDTO response = mapper.toRetrieve(
                patientId,
                List.of(nucleo),
                Map.of(),
                Map.of());

        assertEquals(patientId, response.patientId());
        assertEquals(1, response.nucleoPatient().size());
        assertEquals(List.of(), response.nucleoPatient().get(0).nucleoPatientResponsavel());
        assertEquals(List.of(), response.nucleoPatient().get(0).abordagemId());
    }
}
