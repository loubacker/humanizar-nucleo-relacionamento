package com.humanizar.nucleorelacionamento.infrastructure.messaging.inbound.rabbit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.nucleorelacionamento.application.dto.InboundEnvelopeDTO;
import com.humanizar.nucleorelacionamento.application.mapper.InboundEnvelopeMapper;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.inbound.handler.EventOutcome;
import com.humanizar.nucleorelacionamento.application.messaging.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.nucleorelacionamento.domain.model.enums.ReasonCode;
import com.rabbitmq.client.Channel;

@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    private final ObjectMapper objectMapper;
    private final InboundEnvelopeMapper inboundEnvelopeMapper;
    private final ProcessingResultPublisher processingResultPublisher;

    public DeadLetterConsumer(ObjectMapper objectMapper,
            InboundEnvelopeMapper inboundEnvelopeMapper,
            ProcessingResultPublisher processingResultPublisher) {
        this.objectMapper = objectMapper;
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
        this.processingResultPublisher = processingResultPublisher;
    }

    @RabbitListener(queues = {
            QueueCatalog.NUCLEO_RELACIONAMENTO_ACOLHIMENTO_DLQ,
            QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA_DLQ
    }, containerFactory = "rabbitListenerContainerFactory")
    public void onDeadLetter(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String originalRoutingKey = extractOriginalRoutingKey(message);

        log.error("Mensagem dead-lettered recebida. originalRoutingKey={}, messageId={}, queue={}",
                originalRoutingKey,
                message.getMessageProperties().getMessageId(),
                message.getMessageProperties().getConsumerQueue());

        try {
            InboundEnvelopeDTO<Object> envelopeDto = objectMapper.readValue(
                    message.getBody(), new TypeReference<>() {
                    });
            InboundEnvelopeDTO<Object> envelope = inboundEnvelopeMapper.toInboundEnvelope(envelopeDto);

            if (originalRoutingKey != null) {
                EventOutcome rejection = EventOutcome.failed(
                        ReasonCode.PERSISTENCE_FAILURE,
                        "dead_lettered_after_max_retries");
                processingResultPublisher.publishRejected(envelope, originalRoutingKey, rejection);
                log.info("Rejeicao publicada para mensagem dead-lettered. eventId={}, correlationId={}",
                        envelope.eventId(), envelope.correlationId());
            }
        } catch (IOException ex) {
            log.error("Falha ao processar mensagem DLQ. Mensagem sera apenas logada e confirmada.", ex);
        }

        channel.basicAck(deliveryTag, false);
    }

    private String extractOriginalRoutingKey(Message message) {
        List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
        if (xDeath != null && !xDeath.isEmpty()) {
            Map<String, ?> first = xDeath.getFirst();
            Object routingKeys = first.get("routing-keys");
            if (routingKeys instanceof List<?> keys && !keys.isEmpty()) {
                return keys.getFirst().toString();
            }
        }
        return message.getMessageProperties().getReceivedRoutingKey();
    }
}
