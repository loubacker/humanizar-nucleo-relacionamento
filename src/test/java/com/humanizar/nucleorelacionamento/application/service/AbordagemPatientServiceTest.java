package com.humanizar.nucleorelacionamento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.domain.model.AbordagemPatient;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;

@ExtendWith(MockitoExtension.class)
class AbordagemPatientServiceTest {

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    @Mock
    private NucleoPatientPort nucleoPatientPort;

    @Captor
    private ArgumentCaptor<List<AbordagemPatient>> abordagensCaptor;

    @Test
    void shouldDeleteAllAbordagensWhenIncomingListIsEmpty() {
        UUID correlationId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID abordagemId = UUID.randomUUID();
        ProgramaDTO programaDTO = new ProgramaDTO(nucleoPatientId, List.of());
        AbordagemPatient abordagemAtual = new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, abordagemId);
        AbordagemPatientService abordagemPatientService =
                new AbordagemPatientService(abordagemPatientPort, nucleoPatientPort);

        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(true);
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(abordagemAtual));

        abordagemPatientService.reconcileAbordagens(List.of(programaDTO), correlationId);

        verify(abordagemPatientPort).deleteByNucleoPatientId(nucleoPatientId);
        verify(abordagemPatientPort, never()).saveAll(any());
    }

    @Test
    void shouldResaveRemainingAbordagensWhenThereAreRemovalsAndRemainingItems() {
        UUID correlationId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID abordagemMantidaId = UUID.randomUUID();
        UUID abordagemRemovidaId = UUID.randomUUID();
        ProgramaDTO programaDTO = new ProgramaDTO(nucleoPatientId, List.of(abordagemMantidaId));
        AbordagemPatient abordagemMantida = new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, abordagemMantidaId);
        AbordagemPatient abordagemRemovida = new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, abordagemRemovidaId);
        AbordagemPatientService abordagemPatientService =
                new AbordagemPatientService(abordagemPatientPort, nucleoPatientPort);

        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(true);
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId))
                .thenReturn(List.of(abordagemMantida, abordagemRemovida));

        abordagemPatientService.reconcileAbordagens(List.of(programaDTO), correlationId);

        verify(abordagemPatientPort).deleteByNucleoPatientId(nucleoPatientId);
        verify(abordagemPatientPort).saveAll(abordagensCaptor.capture());
        assertEquals(1, abordagensCaptor.getValue().size());
        assertEquals(nucleoPatientId, abordagensCaptor.getValue().getFirst().getNucleoPatientId());
        assertEquals(abordagemMantidaId, abordagensCaptor.getValue().getFirst().getAbordagemId());
    }
}
