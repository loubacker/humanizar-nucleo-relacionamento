package com.humanizar.nucleorelacionamento.application.usecase.inbound.programa;

import org.springframework.stereotype.Component;

import java.util.List;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.model.EventMetadata;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.ProgramaPayloadValidator;
import com.humanizar.nucleorelacionamento.application.service.AbordagemPatientService;

@Component
public class ProgramaCreatedUseCase {

    private final ProgramaPayloadValidator payloadValidator;
    private final MessageErrorHandler messageErrorHandler;
    private final AbordagemPatientService abordagemPatientService;

    public ProgramaCreatedUseCase(
            ProgramaPayloadValidator payloadValidator,
            MessageErrorHandler messageErrorHandler,
            AbordagemPatientService abordagemPatientService) {
        this.payloadValidator = payloadValidator;
        this.messageErrorHandler = messageErrorHandler;
        this.abordagemPatientService = abordagemPatientService;
    }

    public EventOutcome execute(
            String consumerName,
            String routingKey,
            InboundEnvelopeDTO<?> envelope,
            List<ProgramaDTO> payload) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;
        EventMetadata metadata = EventMetadata.fromEnvelope(envelope);

        payloadValidator.validateCreated(payload, correlationId);
        return messageErrorHandler.handle(
                consumerName,
                envelope.eventId(),
                envelope.correlationId(),
                routingKey,
                envelope.aggregateType(),
                envelope.aggregateId(),
                metadata.actorId(),
                metadata.userAgent(),
                metadata.originIp(),
                () -> {
                    abordagemPatientService.createAbordagens(
                            payload,
                            envelope.correlationId());
                    return null;
                });
    }
}
