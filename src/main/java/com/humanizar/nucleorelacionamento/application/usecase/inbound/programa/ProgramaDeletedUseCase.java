package com.humanizar.nucleorelacionamento.application.usecase.inbound.programa;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.model.EventMetadata;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.ProgramaPayloadValidator;
import com.humanizar.nucleorelacionamento.application.service.AbordagemPatientService;

@Component
public class ProgramaDeletedUseCase {

    private final ProgramaPayloadValidator payloadValidator;
    private final MessageErrorHandler messageErrorHandler;
    private final AbordagemPatientService abordagemPatientService;

    public ProgramaDeletedUseCase(
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
            ProgramaDeletedDTO command) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;
        EventMetadata metadata = EventMetadata.fromEnvelope(envelope);

        payloadValidator.validateDeleted(command, correlationId);
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
                    abordagemPatientService.deleteAllAbordagensByPatientId(
                            command.patientId(),
                            envelope.correlationId());
                    return null;
                });
    }
}
