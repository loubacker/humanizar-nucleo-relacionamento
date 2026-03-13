package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class InboundAcolhimentoCreateMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final InboundAcolhimentoCreateMapper mapper = new InboundAcolhimentoCreateMapper(objectMapper);

    @Test
    void shouldMapPayloadWhenValid() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();

        AcolhimentoCreatedDTO dto = new AcolhimentoCreatedDTO(
                patientId,
                List.of(new NucleoPatientDTO(
                        nucleoPatientId,
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));

        InboundEnvelopeDTO<Object> envelope = envelope(dto);

        AcolhimentoCreatedDTO payload = mapper.toPayload(envelope);

        assertEquals(patientId, payload.patientId());
        assertNotNull(payload.nucleoPatient());
        assertEquals(1, payload.nucleoPatient().size());
    }

    @Test
    void shouldThrowInboundDuplicateItemWhenNucleoPatientIdRepeats() {
        UUID repeatedId = UUID.randomUUID();

        AcolhimentoCreatedDTO dto = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(
                        new NucleoPatientDTO(
                                repeatedId,
                                UUID.randomUUID(),
                                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR"))),
                        new NucleoPatientDTO(
                                repeatedId,
                                UUID.randomUUID(),
                                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toPayload(envelope(dto)));

        assertEquals(ReasonCode.INBOUND_DUPLICATE_ITEM, ex.getReasonCode());
    }

    @Test
    void shouldThrowInboundInvalidEnumWhenRoleIsInvalid() {
        AcolhimentoCreatedDTO dto = new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(new NucleoPatientDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "ROLE_QUE_NAO_EXISTE")))));

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toPayload(envelope(dto)));

        assertEquals(ReasonCode.INBOUND_INVALID_ENUM, ex.getReasonCode());
    }

    private InboundEnvelopeDTO<Object> envelope(Object payload) {
        return new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                "cmd.acolhimento.created.v1",
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }
}
