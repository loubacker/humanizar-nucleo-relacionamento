package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;

public interface ProgramaRoutingHandler {

    String routingKey();

    EventOutcome handle(InboundEnvelopeDTO<Object> envelope);
}
