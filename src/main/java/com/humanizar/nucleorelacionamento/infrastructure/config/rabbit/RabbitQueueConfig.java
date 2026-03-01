package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;

@Configuration
public class RabbitQueueConfig {

    private static final int INBOUND_DELIVERY_LIMIT = 3;

    @Bean
    public Queue nucleoRelacionamentoAcolhimentoDlq() {
        return QueueBuilder.durable(QueueCatalog.NUCLEO_RELACIONAMENTO_ACOLHIMENTO_DLQ)
                .quorum()
                .build();
    }

    @Bean
    public Queue nucleoRelacionamentoAcolhimentoQueue() {
        return QueueBuilder.durable(QueueCatalog.NUCLEO_RELACIONAMENTO_ACOLHIMENTO)
                .quorum()
                .withArgument("x-delivery-limit", INBOUND_DELIVERY_LIMIT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(QueueCatalog.NUCLEO_RELACIONAMENTO_ACOLHIMENTO_DLQ)
                .build();
    }

    @Bean
    public Queue nucleoRelacionamentoProgramaDlq() {
        return QueueBuilder.durable(QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA_DLQ)
                .quorum()
                .build();
    }

    @Bean
    public Queue nucleoRelacionamentoProgramaQueue() {
        return QueueBuilder.durable(QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA)
                .quorum()
                .withArgument("x-delivery-limit", INBOUND_DELIVERY_LIMIT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(QueueCatalog.NUCLEO_RELACIONAMENTO_PROGRAMA_DLQ)
                .build();
    }
}
