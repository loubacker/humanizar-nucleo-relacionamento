package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoCreatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoCreateMapper;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoCreatedUseCase;

@Component
public class AcolhimentoCreatedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoCreateMapper inboundAcolhimentoCreateMapper;
    private final AcolhimentoCreatedUseCase acolhimentoCreatedUseCase;

    public AcolhimentoCreatedRoutingHandler(
            InboundAcolhimentoCreateMapper inboundAcolhimentoCreateMapper,
            AcolhimentoCreatedUseCase acolhimentoCreatedUseCase) {
        this.inboundAcolhimentoCreateMapper = inboundAcolhimentoCreateMapper;
        this.acolhimentoCreatedUseCase = acolhimentoCreatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        AcolhimentoCreatedDTO payload = inboundAcolhimentoCreateMapper.toPayload(envelope);
        return acolhimentoCreatedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
