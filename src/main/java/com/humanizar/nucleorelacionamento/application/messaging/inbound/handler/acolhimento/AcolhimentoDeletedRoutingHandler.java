package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoDeletedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoDeleteMapper;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoDeletedUseCase;

@Component
public class AcolhimentoDeletedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoDeleteMapper inboundAcolhimentoDeleteMapper;
    private final AcolhimentoDeletedUseCase acolhimentoDeletedUseCase;

    public AcolhimentoDeletedRoutingHandler(
            InboundAcolhimentoDeleteMapper inboundAcolhimentoDeleteMapper,
            AcolhimentoDeletedUseCase acolhimentoDeletedUseCase) {
        this.inboundAcolhimentoDeleteMapper = inboundAcolhimentoDeleteMapper;
        this.acolhimentoDeletedUseCase = acolhimentoDeletedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_DELETED_V2;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        AcolhimentoDeletedDTO payload = inboundAcolhimentoDeleteMapper.toPayload(envelope);
        return acolhimentoDeletedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
