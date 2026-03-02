package com.humanizar.nucleorelacionamento.application.usecase.inbound.programa;

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
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.ProgramaPayloadValidator;
import com.humanizar.nucleorelacionamento.application.service.AbordagemPatientService;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;

@ExtendWith(MockitoExtension.class)
class ProgramaUpdatedUseCaseTest {

    @Mock
    private ProgramaPayloadValidator payloadValidator;

    @Mock
    private MessageErrorHandler messageErrorHandler;

    @Mock
    private AbordagemPatientService abordagemPatientService;

    @InjectMocks
    private ProgramaUpdatedUseCase useCase;

    @Test
    void shouldValidateAndReconcileAbordagens() {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();

        InboundEnvelopeDTO<Object> envelope = new InboundEnvelopeDTO<>(
                eventId,
                correlationId,
                "humanizar-programa",
                "humanizar.programa.event",
                RoutingKeyCatalog.PROGRAMA_UPDATED_V1,
                "programa",
                aggregateId,
                1,
                LocalDateTime.now(),
                actorId,
                "JUnit",
                "127.0.0.1",
                new Object());

        List<ProgramaDTO> payload = List.of(new ProgramaDTO(nucleoPatientId, List.of(UUID.randomUUID())));

        when(messageErrorHandler.handle(
                anyString(), any(), any(), anyString(), anyString(), any(), any(), anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Void> action = invocation.getArgument(9, Supplier.class);
                    action.get();
                    return EventOutcome.success();
                });

        EventOutcome outcome = useCase.execute(
                "programa-atendimento-consumer",
                RoutingKeyCatalog.PROGRAMA_UPDATED_V1,
                envelope,
                payload);

        verify(payloadValidator).validateUpdated(payload, correlationId.toString());
        verify(abordagemPatientService).reconcileAbordagens(payload, correlationId);
        assertEquals(ProcessedResult.SUCCESS, outcome.result());
    }
}
