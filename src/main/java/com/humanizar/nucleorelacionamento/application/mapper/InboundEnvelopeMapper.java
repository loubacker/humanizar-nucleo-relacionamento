package com.humanizar.nucleorelacionamento.application.mapper;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.InboundEnvelope;

@Component
public class InboundEnvelopeMapper {

    public <T> InboundEnvelope<T> toCommandEnvelope(InboundEnvelopeDTO<T> inboundEnvelopeDTO) {
        return new InboundEnvelope<>(
                inboundEnvelopeDTO.eventId(),
                inboundEnvelopeDTO.correlationId(),
                inboundEnvelopeDTO.producerService(),
                inboundEnvelopeDTO.exchangeName(),
                inboundEnvelopeDTO.routingKey(),
                inboundEnvelopeDTO.aggregateType(),
                inboundEnvelopeDTO.aggregateId(),
                inboundEnvelopeDTO.eventVersion(),
                inboundEnvelopeDTO.occurredAt(),
                inboundEnvelopeDTO.actorId(),
                inboundEnvelopeDTO.userAgent(),
                inboundEnvelopeDTO.originIp(),
                inboundEnvelopeDTO.payload());
    }
}
