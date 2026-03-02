package com.humanizar.nucleorelacionamento.application.usecase.inbound.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.MessageErrorHandler;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.model.EventMetadata;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.validator.AcolhimentoPayloadValidator;
import com.humanizar.nucleorelacionamento.application.service.NucleoPatientService;

@Component
public class AcolhimentoCreatedUseCase {

    private final AcolhimentoPayloadValidator payloadValidator;
    private final MessageErrorHandler messageErrorHandler;
    private final NucleoPatientService nucleoPatientService;

    public AcolhimentoCreatedUseCase(
            AcolhimentoPayloadValidator payloadValidator,
            MessageErrorHandler messageErrorHandler,
            NucleoPatientService nucleoPatientService) {
        this.payloadValidator = payloadValidator;
        this.messageErrorHandler = messageErrorHandler;
        this.nucleoPatientService = nucleoPatientService;
    }

    public EventOutcome execute(
            String consumerName,
            String routingKey,
            InboundEnvelopeDTO<?> envelope,
            AcolhimentoCreatedDTO command) {
        String correlationId = envelope.correlationId() != null
                ? envelope.correlationId().toString()
                : null;
        EventMetadata metadata = EventMetadata.fromEnvelope(envelope);

        payloadValidator.validateCreated(command, correlationId);
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
                    nucleoPatientService.applyNucleoPatientSnapshot(
                            command.patientId(),
                            command.nucleoPatient(),
                            envelope.correlationId(),
                            metadata.actorId(),
                            metadata.userAgent(),
                            metadata.originIp());
                    return null;
                });
    }
}
