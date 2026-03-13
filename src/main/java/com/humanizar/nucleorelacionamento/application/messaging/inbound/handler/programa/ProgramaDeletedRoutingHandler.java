package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDeletedDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaDeleteMapper;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaDeletedUseCase;

@Component
public class ProgramaDeletedRoutingHandler implements ProgramaRoutingHandler {

    private final InboundProgramaDeleteMapper inboundProgramaDeleteMapper;
    private final ProgramaDeletedUseCase programaDeletedUseCase;

    public ProgramaDeletedRoutingHandler(
            InboundProgramaDeleteMapper inboundProgramaDeleteMapper,
            ProgramaDeletedUseCase programaDeletedUseCase) {
        this.inboundProgramaDeleteMapper = inboundProgramaDeleteMapper;
        this.programaDeletedUseCase = programaDeletedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.PROGRAMA_DELETED_V1;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        ProgramaDeletedDTO payload = inboundProgramaDeleteMapper.toPayload(envelope);
        return programaDeletedUseCase.execute(
                ConsumerCatalog.PROGRAMA_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
