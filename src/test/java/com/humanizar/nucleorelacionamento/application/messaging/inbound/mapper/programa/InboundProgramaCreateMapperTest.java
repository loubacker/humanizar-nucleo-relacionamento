package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class InboundProgramaCreateMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final InboundProgramaCreateMapper mapper = new InboundProgramaCreateMapper(objectMapper);

    @Test
    void shouldMapPayloadWhenValid() {
        List<ProgramaDTO> source = List.of(new ProgramaDTO(UUID.randomUUID(), List.of(UUID.randomUUID())));

        List<ProgramaDTO> payload = mapper.toPayload(envelope(source));

        assertNotNull(payload);
        assertEquals(1, payload.size());
    }

    @Test
    void shouldThrowInboundEmptyCollectionWhenPayloadIsEmpty() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toPayload(envelope(List.of())));

        assertEquals(ReasonCode.INBOUND_EMPTY_COLLECTION, ex.getReasonCode());
    }

    @Test
    void shouldThrowInboundDuplicateItemWhenAbordagemRepeats() {
        UUID abordagemId = UUID.randomUUID();
        List<ProgramaDTO> source = List.of(new ProgramaDTO(UUID.randomUUID(), List.of(abordagemId, abordagemId)));

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.toPayload(envelope(source)));

        assertEquals(ReasonCode.INBOUND_DUPLICATE_ITEM, ex.getReasonCode());
    }

    private InboundEnvelopeDTO<Object> envelope(Object payload) {
        return new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-programa",
                "humanizar.programa.command",
                "cmd.programa.created.v1",
                "programa",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }
}
