package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;

@Configuration
public class RabbitExchangeConfig {

    @Bean
    public TopicExchange acolhimentoExchange() {
        return new TopicExchange(ExchangeCatalog.ACOLHIMENTO_COMMAND, true, false);
    }

    @Bean
    public TopicExchange acolhimentoEventExchange() {
        return new TopicExchange(ExchangeCatalog.ACOLHIMENTO_EVENT, true, false);
    }

    @Bean
    public TopicExchange programaExchange() {
        return new TopicExchange(ExchangeCatalog.PROGRAMA_COMMAND, true, false);
    }

    @Bean
    public TopicExchange programaEventExchange() {
        return new TopicExchange(ExchangeCatalog.PROGRAMA_EVENT, true, false);
    }

    @Bean
    public TopicExchange nucleoRelacionamentoExchange() {
        return new TopicExchange(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT, true, false);
    }
}
