package com.humanizar.nucleorelacionamento.infrastructure.config;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.QueueCatalog;
import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    private static final int INBOUND_DELIVERY_LIMIT = 3;

    // --- Exchanges ---

    @Bean
    public TopicExchange acolhimentoExchange() {
        return new TopicExchange(ExchangeCatalog.ACOLHIMENTO_EVENT, true, false);
    }

    @Bean
    public TopicExchange programaExchange() {
        return new TopicExchange(ExchangeCatalog.PROGRAMA_EVENT, true, false);
    }

    @Bean
    public TopicExchange nucleoRelacionamentoExchange() {
        return new TopicExchange(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT, true, false);
    }

    // --- Queues ---

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

    // --- Bindings inbound: acolhimento → nucleo-relacionamento ---

    @Bean
    public Binding bindAcolhimentoCreated(@Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
                                          TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
    }

    @Bean
    public Binding bindAcolhimentoUpdated(@Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
                                          TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1);
    }

    @Bean
    public Binding bindAcolhimentoDeleted(@Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
                                          TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1);
    }

    // --- Bindings inbound: programa → nucleo-relacionamento ---

    @Bean
    public Binding bindProgramaCreated(@Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
                                       TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_CREATED_V1);
    }

    @Bean
    public Binding bindProgramaUpdated(@Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
                                       TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_UPDATED_V1);
    }

    @Bean
    public Binding bindProgramaDeleted(@Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
                                       TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_DELETED_V1);
    }

}
