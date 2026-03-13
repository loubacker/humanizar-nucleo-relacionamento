package com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

class EnvelopeInboundMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final EnvelopeInboundMapper mapper = new EnvelopeInboundMapper(objectMapper);

    @Test
    void shouldParseAndValidateWhenEnvelopeIsComplete() throws Exception {
        InboundEnvelopeDTO<Object> envelope = new InboundEnvelopeDTO<>(
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
                java.util.Map.of("patientId", UUID.randomUUID().toString()));

        byte[] body = objectMapper.writeValueAsBytes(envelope);
        InboundEnvelopeDTO<Object> parsed = mapper.parseEnvelope(body);

        mapper.validate(parsed);

        assertNotNull(parsed.eventId());
        assertNotNull(parsed.correlationId());
    }

    @Test
    void shouldThrowInboundRequiredFieldWhenActorIsMissing() {
        InboundEnvelopeDTO<Object> envelope = new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                "cmd.acolhimento.created.v1",
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                null,
                "JUnit",
                "127.0.0.1",
                new Object());

        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.validate(envelope));

        assertEquals(ReasonCode.INBOUND_REQUIRED_FIELD, ex.getReasonCode());
    }

    @Test
    void shouldThrowInboundParseErrorWhenJsonIsInvalid() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> mapper.parseEnvelope("{invalid json}".getBytes()));

        assertEquals(ReasonCode.INBOUND_PARSE_ERROR, ex.getReasonCode());
    }
}
