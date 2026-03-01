package com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.command.InboundEnvelope;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.event.ProcessingResultEnvelopeEvent;
import com.humanizar.nucleorelacionamento.domain.exception.NucleoRelacionamentoException;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;

@Component
public class ProcessingResultPublisher {

        private static final Logger log = LoggerFactory.getLogger(ProcessingResultPublisher.class);
        private static final String PRODUCER_SERVICE = "humanizar-nucleo-relacionamento";
        private static final String STATUS_PROCESSED = "PROCESSED";
        private static final String STATUS_REJECTED = "REJECTED";

        private final OutboxEventPublisher outboxEventPublisher;

        public ProcessingResultPublisher(OutboxEventPublisher outboxEventPublisher) {
                this.outboxEventPublisher = outboxEventPublisher;
        }

        public void publishProcessed(InboundEnvelope<?> inboundEnvelope, String upStreamRoutingKey) {
                String processedRoutingKey = resolveProcessedRoutingKey(upStreamRoutingKey, inboundEnvelope);
                LocalDateTime now = LocalDateTime.now();

                ProcessingResultEnvelopeEvent envelopeEvent = new ProcessingResultEnvelopeEvent(
                                upStreamRoutingKey,
                                inboundEnvelope.eventId(),
                                inboundEnvelope.correlationId(),
                                PRODUCER_SERVICE,
                                ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT,
                                processedRoutingKey,
                                inboundEnvelope.aggregateType(),
                                inboundEnvelope.aggregateId(),
                                inboundEnvelope.eventVersion(),
                                now,
                                inboundEnvelope.actorId(),
                                inboundEnvelope.userAgent(),
                                inboundEnvelope.originIp(),
                                STATUS_PROCESSED,
                                null,
                                null,
                                now,
                                null);

                outboxEventPublisher.publish(
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

        public void publishRejected(InboundEnvelope<?> inboundEnvelope,
                        String upStreamRoutingKey,
                        EventOutcome eventOutcome) {
                String rejectedRoutingKey = resolveRejectedRoutingKey(upStreamRoutingKey, inboundEnvelope);
                LocalDateTime now = LocalDateTime.now();

                String reasonCode = eventOutcome.reasonCode() != null ? eventOutcome.reasonCode().name() : null;

                ProcessingResultEnvelopeEvent envelopeEvent = new ProcessingResultEnvelopeEvent(
                                upStreamRoutingKey,
                                inboundEnvelope.eventId(),
                                inboundEnvelope.correlationId(),
                                PRODUCER_SERVICE,
                                ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT,
                                rejectedRoutingKey,
                                inboundEnvelope.aggregateType(),
                                inboundEnvelope.aggregateId(),
                                inboundEnvelope.eventVersion(),
                                now,
                                inboundEnvelope.actorId(),
                                inboundEnvelope.userAgent(),
                                inboundEnvelope.originIp(),
                                STATUS_REJECTED,
                                reasonCode,
                                eventOutcome.errorMessage(),
                                null,
                                now);

                outboxEventPublisher.publish(
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

        private String resolveProcessedRoutingKey(String upStreamRoutingKey, InboundEnvelope<?> inboundEnvelope) {
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

        private String resolveRejectedRoutingKey(String upStreamRoutingKey, InboundEnvelope<?> inboundEnvelope) {
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
}
