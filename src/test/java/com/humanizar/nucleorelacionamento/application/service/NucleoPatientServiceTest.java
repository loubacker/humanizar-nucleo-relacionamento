package com.humanizar.nucleorelacionamento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelDesvinculadoDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.OutboundResponsavelVinculadoDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper.OutboundResponsavelDesvinculadoMapper;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper.OutboundResponsavelVinculadoMapper;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.OutboxEventPublisher;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatient;
import com.humanizar.nucleorelacionamento.domain.model.NucleoPatientResponsavel;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.model.enums.ResponsavelRole;
import com.humanizar.nucleorelacionamento.domain.port.AbordagemPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientPort;
import com.humanizar.nucleorelacionamento.domain.port.NucleoPatientResponsavelPort;

@ExtendWith(MockitoExtension.class)
class NucleoPatientServiceTest {

    @Mock
    private NucleoPatientPort nucleoPatientPort;

    @Mock
    private NucleoPatientResponsavelPort responsavelPort;

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Spy
    private OutboundResponsavelVinculadoMapper outboundResponsavelVinculadoMapper;

    @Spy
    private OutboundResponsavelDesvinculadoMapper outboundResponsavelDesvinculadoMapper;

    @InjectMocks
    private NucleoPatientService service;

    @Test
    void shouldCreateNucleoWithProvidedNucleoPatientId() {
        UUID nucleoPatientId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(false);
        when(nucleoPatientPort.findByPatientIdAndNucleoId(patientId, nucleoId)).thenReturn(Optional.empty());
        when(nucleoPatientPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0, NucleoPatient.class));

        service.createNucleoPatient(
                nucleoPatientId,
                patientId,
                nucleoId,
                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")),
                correlationId,
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        ArgumentCaptor<NucleoPatient> captor = ArgumentCaptor.forClass(NucleoPatient.class);
        verify(nucleoPatientPort).save(captor.capture());
        assertEquals(nucleoPatientId, captor.getValue().getId());
        assertEquals(patientId, captor.getValue().getPatientId());
        assertEquals(nucleoId, captor.getValue().getNucleoId());

        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT),
                eq(RoutingKeyCatalog.RESPONSAVEL_VINCULADO_V1),
                eq("nucleo_patient"),
                eq(nucleoPatientId),
                eventIdCaptor.capture(),
                eq(correlationId),
                payloadCaptor.capture(),
                any(),
                any(),
                any());

        @SuppressWarnings("unchecked")
        OutboundEnvelopeDTO<OutboundResponsavelVinculadoDTO> envelope =
                (OutboundEnvelopeDTO<OutboundResponsavelVinculadoDTO>) payloadCaptor.getValue();
        assertEquals(eventIdCaptor.getValue(), envelope.eventId());
        assertEquals(correlationId, envelope.correlationId());
        assertEquals("humanizar-nucleo-relacionamento", envelope.producerService());
    }

    @Test
    void shouldRejectCreateWhenNucleoPatientIdAlreadyExists() {
        UUID nucleoPatientId = UUID.randomUUID();
        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(true);

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> service.createNucleoPatient(
                        nucleoPatientId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR")),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1"));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
    }

    @Test
    void shouldRejectSnapshotWhenNucleoIdDiffersForSameNucleoPatientId() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID persistedNucleoId = UUID.randomUUID();
        UUID incomingNucleoId = UUID.randomUUID();

        NucleoPatient persisted = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(persistedNucleoId)
                .build();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(persisted));

        NucleoRelacionamentoException exception = assertThrows(
                NucleoRelacionamentoException.class,
                () -> service.applyNucleoPatientSnapshot(
                        patientId,
                        List.of(new NucleoPatientDTO(
                                nucleoPatientId,
                                incomingNucleoId,
                                List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1"));

        assertEquals(ReasonCode.VALIDATION_ERROR, exception.getReasonCode());
    }

    @Test
    void shouldPublishEnvelopeWhenDeletingResponsaveis() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        NucleoPatient persisted = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();
        NucleoPatientResponsavel responsavel = new NucleoPatientResponsavel(
                UUID.randomUUID(),
                nucleoPatientId,
                responsavelId,
                ResponsavelRole.ADMINISTRADOR);

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(persisted));
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(new ArrayList<>());
        when(responsavelPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(responsavel));

        service.deleteAllNucleosByPatientId(
                patientId,
                correlationId,
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT),
                eq(RoutingKeyCatalog.RESPONSAVEL_DESVINCULADO_V1),
                eq("nucleo_patient"),
                eq(nucleoPatientId),
                eventIdCaptor.capture(),
                eq(correlationId),
                payloadCaptor.capture(),
                any(),
                any(),
                any());

        @SuppressWarnings("unchecked")
        OutboundEnvelopeDTO<OutboundResponsavelDesvinculadoDTO> envelope =
                (OutboundEnvelopeDTO<OutboundResponsavelDesvinculadoDTO>) payloadCaptor.getValue();
        assertEquals(eventIdCaptor.getValue(), envelope.eventId());
        assertEquals(correlationId, envelope.correlationId());
        assertEquals("humanizar-nucleo-relacionamento", envelope.producerService());
    }
}
