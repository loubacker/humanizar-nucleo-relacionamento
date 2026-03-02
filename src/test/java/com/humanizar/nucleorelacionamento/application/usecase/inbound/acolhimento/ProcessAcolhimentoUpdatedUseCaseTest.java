package com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.AcolhimentoPayloadValidator;
import com.humanizar.nucleorelacionamento.application.service.NucleoPatientService;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;

@ExtendWith(MockitoExtension.class)
class ProcessAcolhimentoUpdatedUseCaseTest {

        @Mock
        private AcolhimentoPayloadValidator payloadValidator;

        @Mock
        private MessageErrorHandler messageErrorHandler;

        @Mock
        private NucleoPatientService nucleoPatientService;

        @InjectMocks
        private AcolhimentoUpdatedUseCase useCase;

        @Test
        void shouldValidateAndApplySnapshotForUpdated() {
                UUID eventId = UUID.randomUUID();
                UUID correlationId = UUID.randomUUID();
                UUID aggregateId = UUID.randomUUID();
                UUID actorId = UUID.randomUUID();
                UUID patientId = UUID.randomUUID();

                InboundEnvelopeDTO<Object> envelope = new InboundEnvelopeDTO<>(
                                eventId,
                                correlationId,
                                "humanizar-acolhimento",
                                "humanizar.acolhimento.event",
                                RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1,
                                "acolhimento",
                                aggregateId,
                                1,
                                LocalDateTime.now(),
                                actorId,
                                "JUnit",
                                "127.0.0.1",
                                new Object());

                AcolhimentoUpdatedDTO command = new AcolhimentoUpdatedDTO(
                                patientId,
                                List.of(new NucleoPatientDTO(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                List.of(new ResponsavelDTO(UUID.randomUUID(), "ADMINISTRADOR")))));

                when(messageErrorHandler.handle(
                                anyString(), any(), any(), anyString(), anyString(), any(), any(), anyString(),
                                anyString(),
                                any()))
                                .thenAnswer(invocation -> {
                                        @SuppressWarnings("unchecked")
                                        Supplier<Void> action = invocation.getArgument(9, Supplier.class);
                                        action.get();
                                        return EventOutcome.success();
                                });

                EventOutcome outcome = useCase.execute(
                                "acolhimento-consumer",
                                RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1,
                                envelope,
                                command);

                verify(payloadValidator).validateUpdated(command, correlationId.toString());
                verify(nucleoPatientService).applyNucleoPatientSnapshot(
                                patientId,
                                command.nucleoPatient(),
                                correlationId,
                                actorId,
                                "JUnit",
                                "127.0.0.1");
                assertEquals(ProcessedResult.SUCCESS, outcome.result());
        }
}
