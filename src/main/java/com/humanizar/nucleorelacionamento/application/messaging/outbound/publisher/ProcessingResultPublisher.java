package com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.dto.CallbackDTO;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper.OutboundCallbackMapper;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class ProcessingResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProcessingResultPublisher.class);

    private final OutboxEventPublisher outboxEventPublisher;
    private final OutboundCallbackMapper outboundCallbackMapper;

    public ProcessingResultPublisher(
            OutboxEventPublisher outboxEventPublisher,
            OutboundCallbackMapper outboundCallbackMapper) {
        this.outboxEventPublisher = outboxEventPublisher;
        this.outboundCallbackMapper = outboundCallbackMapper;
    }

    public void publishProcessed(InboundEnvelopeDTO<?> inboundEnvelope, String upStreamRoutingKey) {
        String processedRoutingKey = resolveProcessedRoutingKey(upStreamRoutingKey, inboundEnvelope);
        String callbackExchange = resolveCallbackExchange(upStreamRoutingKey, inboundEnvelope);
        CallbackDTO envelopeEvent = outboundCallbackMapper.toProcessedCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                processedRoutingKey);

        outboxEventPublisher.publish(
                callbackExchange,
                processedRoutingKey,
                inboundEnvelope.aggregateType(),
                inboundEnvelope.aggregateId(),
                inboundEnvelope.eventId(),
                inboundEnvelope.correlationId(),
                envelopeEvent,
                inboundEnvelope.actorId(),
                inboundEnvelope.userAgent(),
                inboundEnvelope.originIp());

        log.info(
                "Confirmacao PROCESSED enfileirada no outbox. upStream={}, routingKey={}, eventId={}, correlationId={}",
                upStreamRoutingKey, processedRoutingKey, inboundEnvelope.eventId(),
                inboundEnvelope.correlationId());
    }

    public void publishRejected(InboundEnvelopeDTO<?> inboundEnvelope,
            String upStreamRoutingKey,
            EventOutcome eventOutcome) {
        String rejectedRoutingKey = resolveRejectedRoutingKey(upStreamRoutingKey, inboundEnvelope);
        String callbackExchange = resolveCallbackExchange(upStreamRoutingKey, inboundEnvelope);
        String reasonCode = eventOutcome.reasonCode() != null ? eventOutcome.reasonCode().name() : null;
        CallbackDTO envelopeEvent = outboundCallbackMapper.toRejectedCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                rejectedRoutingKey,
                reasonCode,
                eventOutcome.errorMessage());

        outboxEventPublisher.publish(
                callbackExchange,
                rejectedRoutingKey,
                inboundEnvelope.aggregateType(),
                inboundEnvelope.aggregateId(),
                inboundEnvelope.eventId(),
                inboundEnvelope.correlationId(),
                envelopeEvent,
                inboundEnvelope.actorId(),
                inboundEnvelope.userAgent(),
                inboundEnvelope.originIp());

        log.warn(
                "Confirmacao REJECTED enfileirada no outbox. upStream={}, routingKey={}, reasonCode={}, eventId={}, correlationId={}",
                upStreamRoutingKey, rejectedRoutingKey, reasonCode, inboundEnvelope.eventId(),
                inboundEnvelope.correlationId());
    }

    private String resolveProcessedRoutingKey(String upStreamRoutingKey, InboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1;
        }
        if (RoutingKeyCatalog.isProgramaInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.PROGRAMA_PROCESSED_V1;
        }
        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new NucleoRelacionamentoException(ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId,
                "Routing key de upstream nao suportada para confirmacao processed: "
                        + upStreamRoutingKey);
    }

    private String resolveRejectedRoutingKey(String upStreamRoutingKey, InboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.ACOLHIMENTO_REJECTED_V1;
        }
        if (RoutingKeyCatalog.isProgramaInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.PROGRAMA_REJECTED_V1;
        }
        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new NucleoRelacionamentoException(ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId,
                "Routing key de upstream nao suportada para confirmacao rejected: "
                        + upStreamRoutingKey);
    }

    private String resolveCallbackExchange(String upStreamRoutingKey, InboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return ExchangeCatalog.ACOLHIMENTO_EVENT;
        }
        if (RoutingKeyCatalog.isProgramaInbound(upStreamRoutingKey)) {
            return ExchangeCatalog.PROGRAMA_EVENT;
        }
        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new NucleoRelacionamentoException(ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId,
                "Routing key de upstream nao suportada para resolucao de exchange de callback: "
                        + upStreamRoutingKey);
    }
}
