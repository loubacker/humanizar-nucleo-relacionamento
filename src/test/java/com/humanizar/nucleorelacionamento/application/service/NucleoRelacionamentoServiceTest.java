package com.humanizar.nucleorelacionamento.application.service;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.retrieve.NucleoRelacionamentoRetrieveDTO;
import com.humanizar.nucleorelacionamento.application.mapper.NucleoRelacionamentoMapper;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientResponsavelPort;

@ExtendWith(MockitoExtension.class)
class NucleoRelacionamentoServiceTest {

    @Mock
    private NucleoPatientPort nucleoPatientPort;

    @Mock
    private NucleoPatientResponsavelPort nucleoPatientResponsavelPort;

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    private NucleoRelacionamentoService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new NucleoRelacionamentoService(
                nucleoPatientPort,
                nucleoPatientResponsavelPort,
                abordagemPatientPort,
                new NucleoRelacionamentoMapper());
    }

    @Test
    void shouldReturnRetrievePayloadWhenPatientHasNucleos() {
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

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(nucleo));
        when(nucleoPatientResponsavelPort.findAllResponsaveisByNucleoPatientId(List.of(nucleoPatientId)))
                .thenReturn(List.of(responsavel));
        when(abordagemPatientPort.findAllAbordagensByNucleoPatientId(List.of(nucleoPatientId)))
                .thenReturn(List.of(abordagem));

        NucleoRelacionamentoRetrieveDTO response = service.findByPatientId(patientId);

        assertEquals(patientId, response.patientId());
        assertEquals(1, response.nucleoPatient().size());
        assertEquals(nucleoPatientId, response.nucleoPatient().get(0).nucleoPatientId());
        assertEquals(List.of(abordagemId), response.nucleoPatient().get(0).abordagemId());
        verify(nucleoPatientResponsavelPort).findAllResponsaveisByNucleoPatientId(List.of(nucleoPatientId));
        verify(abordagemPatientPort).findAllAbordagensByNucleoPatientId(List.of(nucleoPatientId));
    }

    @Test
    void shouldThrowPatientNotFoundWhenPatientHasNoNucleos() {
        UUID patientId = UUID.randomUUID();
        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of());

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> service.findByPatientId(patientId));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, exception.getReasonCode());
    }

    @Test
    void shouldThrowValidationErrorWhenPatientIdIsNull() {
        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> service.findByPatientId(null));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
    }
}
