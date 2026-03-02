package com.humanizar.nucleorelacionamento.application.mapper;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;

@Component
public class InboundEnvelopeMapper {

    public <T> InboundEnvelopeDTO<T> toInboundEnvelope(InboundEnvelopeDTO<T> inboundEnvelopeDTO) {
        return inboundEnvelopeDTO;
    }
}
