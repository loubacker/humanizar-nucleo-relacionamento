package com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.programa;

import java.util.List;

import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.dto.programa.ProgramaDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ConsumerCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.mapper.programa.InboundProgramaUpdateMapper;
import com.humanizar.nucleorelacionamento.application.usecase.programa.ProgramaUpdatedUseCase;

@Component
public class ProgramaUpdatedRoutingHandler implements ProgramaRoutingHandler {

    private final InboundProgramaUpdateMapper inboundProgramaUpdateMapper;
    private final ProgramaUpdatedUseCase programaUpdatedUseCase;

    public ProgramaUpdatedRoutingHandler(
            InboundProgramaUpdateMapper inboundProgramaUpdateMapper,
            ProgramaUpdatedUseCase programaUpdatedUseCase) {
        this.inboundProgramaUpdateMapper = inboundProgramaUpdateMapper;
        this.programaUpdatedUseCase = programaUpdatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.PROGRAMA_UPDATED_V1;
    }

    @Override
    public EventOutcome handle(InboundEnvelopeDTO<Object> envelope) {
        List<ProgramaDTO> payload = inboundProgramaUpdateMapper.toPayload(envelope);
        return programaUpdatedUseCase.execute(
                ConsumerCatalog.PROGRAMA_CONSUMER,
                routingKey(),
                envelope,
                payload);
    }
}
