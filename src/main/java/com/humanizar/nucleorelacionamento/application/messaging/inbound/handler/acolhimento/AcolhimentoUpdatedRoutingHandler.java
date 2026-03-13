package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.acolhimento.AcolhimentoUpdatedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.acolhimento.InboundAcolhimentoUpdateMapper;
import com.humanizar.nucleorelacionamento.application.usecase.acolhimento.AcolhimentoUpdatedUseCase;

@Component
public class AcolhimentoUpdatedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoUpdateMapper inboundAcolhimentoUpdateMapper;
    private final AcolhimentoUpdatedUseCase acolhimentoUpdatedUseCase;

    public AcolhimentoUpdatedRoutingHandler(
            InboundAcolhimentoUpdateMapper inboundAcolhimentoUpdateMapper,
            AcolhimentoUpdatedUseCase acolhimentoUpdatedUseCase) {
        this.inboundAcolhimentoUpdateMapper = inboundAcolhimentoUpdateMapper;
        this.acolhimentoUpdatedUseCase = acolhimentoUpdatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        AcolhimentoUpdatedDTO payload = inboundAcolhimentoUpdateMapper.toPayload(envelope);
        return acolhimentoUpdatedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
