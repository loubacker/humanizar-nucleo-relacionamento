package com.humanizar.nucleorelacionamento.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;

class AcolhimentoInboundMapperTest {

    private final AcolhimentoInboundMapper mapper = new AcolhimentoInboundMapper();

    @Test
    void shouldMapCreatedPayloadAsSnapshot() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        AcolhimentoCreatedDTO dto = new AcolhimentoCreatedDTO(
                patientId,
                List.of(new NucleoPatientDTO(
                        nucleoPatientId,
                        nucleoId,
                        List.of(new ResponsavelDTO(responsavelId, "ADMINISTRADOR")))));

        AcolhimentoCreatedDTO command = mapper.toCreatedPayload(dto);

        assertEquals(patientId, command.patientId());
        assertNotNull(command.nucleoPatient());
        assertEquals(1, command.nucleoPatient().size());
        assertEquals(nucleoPatientId, command.nucleoPatient().get(0).nucleoPatientId());
        assertEquals(nucleoId, command.nucleoPatient().get(0).nucleoId());
        assertEquals(1, command.nucleoPatient().get(0).nucleoPatientResponsavel().size());
        assertEquals(responsavelId, command.nucleoPatient().get(0).nucleoPatientResponsavel().get(0).responsavelId());
    }
}
