package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaCreateMapper;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaCreatedUseCase;

@Component
public class ProgramaCreatedRoutingHandler implements ProgramaRoutingHandler {

    private final InboundProgramaCreateMapper inboundProgramaCreateMapper;
    private final ProgramaCreatedUseCase programaCreatedUseCase;

    public ProgramaCreatedRoutingHandler(
            InboundProgramaCreateMapper inboundProgramaCreateMapper,
            ProgramaCreatedUseCase programaCreatedUseCase) {
        this.inboundProgramaCreateMapper = inboundProgramaCreateMapper;
        this.programaCreatedUseCase = programaCreatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.PROGRAMA_CREATED_V1;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        List<ProgramaDTO> payload = inboundProgramaCreateMapper.toPayload(envelope);
        return programaCreatedUseCase.execute(
                ConsumerCatalog.PROGRAMA_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
