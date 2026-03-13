package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;

public interface AcolhimentoRoutingHandler {

    String routingKey();

    EventOutcome handle(InboundEnvelopeDTO<Object> envelope);
}
