package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;

@Configuration
public class RabbitBindingConfig {

    @Bean
    public Binding bindAcolhimentoCreated(
            @Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
            TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
    }

    @Bean
    public Binding bindAcolhimentoUpdated(
            @Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
            TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1);
    }

    @Bean
    public Binding bindAcolhimentoDeleted(
            @Qualifier("nucleoRelacionamentoAcolhimentoQueue") Queue nucleoRelacionamentoAcolhimentoQueue,
            TopicExchange acolhimentoExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoAcolhimentoQueue)
                .to(acolhimentoExchange)
                .with(RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1);
    }

    @Bean
    public Binding bindProgramaCreated(
            @Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
            TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_CREATED_V1);
    }

    @Bean
    public Binding bindProgramaUpdated(
            @Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
            TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_UPDATED_V1);
    }

    @Bean
    public Binding bindProgramaDeleted(
            @Qualifier("nucleoRelacionamentoProgramaQueue") Queue nucleoRelacionamentoProgramaQueue,
            TopicExchange programaExchange) {
        return BindingBuilder.bind(nucleoRelacionamentoProgramaQueue)
                .to(programaExchange)
                .with(RoutingKeyCatalog.PROGRAMA_DELETED_V1);
    }
}
