package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit.binding;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.RoutingKeyCatalog;

@Configuration
public class RabbitProgramaBindingConfig {

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
