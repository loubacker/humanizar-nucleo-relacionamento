package com.humanizar.nucleorelacionamento.application.messaging.inbound.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class AcolhimentoPayloadValidatorTest {

    private final AcolhimentoPayloadValidator validator = new AcolhimentoPayloadValidator();

    @Test
    void shouldAcceptCreatedSnapshotPayload() {
        AcolhimentoCreatedDTO command = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(new NucleoPatientDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));

        assertDoesNotThrow(() -> validator.validateCreated(command, UUID.randomUUID().toString()));
    }

    @Test
    void shouldRejectCreatedWhenNucleoPatientIsEmpty() {
        AcolhimentoCreatedDTO command = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                Collections.emptyList());

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> validator.validateCreated(command, UUID.randomUUID().toString()));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
        assertEquals("nucleoPatient e obrigatorio", exception.getMessage());
    }

    @Test
    void shouldRejectCreatedWhenNucleoPatientIdIsMissing() {
        AcolhimentoCreatedDTO command = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(new NucleoPatientDTO(
                        null,
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR")))));

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> validator.validateCreated(command, UUID.randomUUID().toString()));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
        assertEquals("nucleoPatient[0].nucleoPatientId e obrigatorio", exception.getMessage());
    }

    @Test
    void shouldRejectCreatedWhenNucleoPatientIdIsDuplicated() {
        UUID duplicatedNucleoPatientId = UUID.randomUUID();
        AcolhimentoCreatedDTO command = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(
                        new NucleoPatientDTO(
                                duplicatedNucleoPatientId,
                                UUID.randomUUID(),
                                List.of(new ResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR"))),
                        new NucleoPatientDTO(
                                duplicatedNucleoPatientId,
                                UUID.randomUUID(),
                                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> validator.validateCreated(command, UUID.randomUUID().toString()));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
        assertEquals("nucleoPatientId duplicado no payload", exception.getMessage());
    }
}
