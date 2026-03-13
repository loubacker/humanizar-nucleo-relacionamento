package com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoCreatedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.NucleoPatientDTO;
import com.humanizar.nucleorelacionamento.application.dto.ResponsavelDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.service.NucleoPatientService;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ProcessedResult;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.humanizar.nucleorelacionamento.domain.port.ProcessedEventPort;

@ExtendWith(MockitoExtension.class)
class AcolhimentoCreatedUseCaseTest {

    @Mock
    private NucleoPatientService nucleoPatientService;

    @Mock
    private ProcessedEventPort processedEventPort;

    private AcolhimentoCreatedUseCase useCase;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        useCase = new AcolhimentoCreatedUseCase(nucleoPatientService, processedEventPort);
    }

    @Test
    void shouldSaveProcessedEventAndReturnSuccess() {
        InboundEnvelopeDTO<Object> envelope = envelope();
        AcolhimentoCreatedDTO payload = payload();

        EventOutcome outcome = useCase.execute("consumer", "cmd.acolhimento.created.v1", envelope, payload);

        assertEquals(ProcessedResult.SUCCESS, outcome.result());
        verify(processedEventPort).save(any());
    }

    @Test
    void shouldReturnFailedAndNotSaveWhenReasonIsRetryable() {
        InboundEnvelopeDTO<Object> envelope = envelope();
        AcolhimentoCreatedDTO payload = payload();

        doThrow(new NucleoRelacionamentoException(ReasonCode.PERSISTENCE_FAILURE, envelope.correlationId().toString()))
                .when(nucleoPatientService).applyNucleoPatientSnapshot(any(), any(), any(), any(), any(), any());

        EventOutcome outcome = useCase.execute("consumer", "cmd.acolhimento.created.v1", envelope, payload);

        assertEquals(ReasonCode.PERSISTENCE_FAILURE, outcome.reasonCode());
        verify(processedEventPort, never()).save(any());
    }

    private InboundEnvelopeDTO<Object> envelope() {
        return new InboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                "cmd.acolhimento.created.v1",
                "acolhimento",
                UUID.randomUUID(),
                1,
                java.time.LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                new Object());
    }

    private AcolhimentoCreatedDTO payload() {
        return new AcolhimentoCreatedDTO(
                UUID.randomUUID(),
                List.of(new NucleoPatientDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(new ResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))));
    }
}
