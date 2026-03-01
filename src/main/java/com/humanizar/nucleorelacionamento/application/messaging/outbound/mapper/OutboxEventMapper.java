package com.humanizar.nucleorelacionamento.application.messaging.outbound.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.domain.model.OutboxEvent;
import com.humanizar.nucleorelacionamento.domain.model.enums.OutboxStatus;

@Component
public class OutboxEventMapper {

    private static final String PRODUCER_SERVICE = "humanizar-nucleo-relacionamento";
    private static final String AGGREGATE_TYPE = "nucleo_patient";
    private static final short EVENT_VERSION = 1;
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    private final ObjectMapper objectMapper;

    public OutboxEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent toOutboxEvent(String routingKey, UUID aggregateId,
            UUID correlationId, Object payload,
            UUID actorId, String userAgent, String originIp) {
        return toOutboxEvent(
                routingKey,
                AGGREGATE_TYPE,
                aggregateId,
                null,
                correlationId,
                payload,
                actorId,
                userAgent,
                originIp);
    }

    public OutboxEvent toOutboxEvent(String routingKey,
            String aggregateType,
            UUID aggregateId,
            UUID eventId,
            UUID correlationId,
            Object payload,
            UUID actorId,
            String userAgent,
            String originIp) {
        String resolvedAggregateType = aggregateType != null && !aggregateType.isBlank()
                ? aggregateType
                : AGGREGATE_TYPE;
        UUID resolvedEventId = eventId != null ? eventId : UUID.randomUUID();

        return OutboxEvent.builder()
                .eventId(resolvedEventId)
                .correlationId(correlationId)
                .producerService(PRODUCER_SERVICE)
                .exchangeName(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT)
                .routingKey(routingKey)
                .aggregateType(resolvedAggregateType)
                .aggregateId(aggregateId)
                .eventVersion(EVENT_VERSION)
                .payload(serialize(payload))
                .actorId(actorId)
                .userAgent(userAgent)
                .originIp(originIp)
                .status(OutboxStatus.NEW)
                .attemptCount(0)
                .maxAttempts(DEFAULT_MAX_ATTEMPTS)
                .nextRetryAt(LocalDateTime.now())
                .build();
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar payload para outbox", e);
        }
    }
}
