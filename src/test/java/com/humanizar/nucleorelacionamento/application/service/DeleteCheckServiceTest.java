package com.humanizar.nucleorelacionamento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.DeleteCheckResponseDTO;
import com.humanizar.nucleorelacionamento.application.mapper.DeleteCheckMapper;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;

@ExtendWith(MockitoExtension.class)
class DeleteCheckServiceTest {

    @Mock
    private NucleoPatientPort nucleoPatientPort;

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    @Spy
    private DeleteCheckMapper deleteCheckMapper;

    @InjectMocks
    private DeleteCheckService service;

    @Test
    void shouldReturnAllowedWhenPatientHasNoNucleos() {
        UUID patientId = UUID.randomUUID();
        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of());

        DeleteCheckResponseDTO response = service.checkDeleteStatusByPatientId(patientId);

        assertEquals(true, response.canDelete());
        assertEquals(List.of(), response.blockedNucleos());
    }

    @Test
    void shouldReturnAllowedWhenPatientHasNucleosWithoutAbordagem() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        NucleoPatient nucleo = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(nucleo));
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of());

        DeleteCheckResponseDTO response = service.checkDeleteStatusByPatientId(patientId);

        assertEquals(true, response.canDelete());
        assertEquals(List.of(), response.blockedNucleos());
    }

    @Test
    void shouldReturnBlockedWhenPatientHasNucleoWithAbordagem() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        NucleoPatient nucleo = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();

        List<AbordagemPatient> abordagens = List.of(
                new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, UUID.randomUUID()),
                new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, UUID.randomUUID()));

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(nucleo));
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(abordagens);

        DeleteCheckResponseDTO response = service.checkDeleteStatusByPatientId(patientId);

        assertEquals(false, response.canDelete());
        assertEquals(ReasonCode.HAS_ABORDAGEM.name(), response.reasonCode());
        assertEquals(1, response.blockedNucleos().size());
        assertEquals(nucleoId, response.blockedNucleos().get(0).nucleoId());
        assertEquals(2, response.blockedNucleos().get(0).abordagemCount());
    }

    @Test
    void shouldThrowValidationErrorWhenPatientIdIsNull() {
        NucleoRelacionamentoException ex = assertThrows(
                NucleoRelacionamentoException.class,
                () -> service.checkDeleteStatusByPatientId(null));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }
}
