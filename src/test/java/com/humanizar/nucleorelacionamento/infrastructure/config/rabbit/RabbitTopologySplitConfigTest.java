package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import com.humanizar.nucleorelacionamento.application.messaging.catalog.ExchangeCatalog;
import com.humanizar.nucleorelacionamento.infrastructure.config.rabbit.binding.RabbitAcolhimentoBindingConfig;
import com.humanizar.nucleorelacionamento.infrastructure.config.rabbit.binding.RabbitProgramaBindingConfig;

class RabbitTopologySplitConfigTest {

    @Test
    void shouldDeclareExchangesQueuesAndBindingsWithConfiguredDeliveryLimit() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context, "app.messaging.inbound.delivery-limit=3");

            context.register(
                    RabbitExchangeConfig.class,
                    RabbitQueueConfig.class,
                    RabbitAcolhimentoBindingConfig.class,
                    RabbitProgramaBindingConfig.class);
            context.refresh();

            assertThat(context.containsBean("acolhimentoExchange")).isTrue();
            assertThat(context.containsBean("acolhimentoEventExchange")).isTrue();
            assertThat(context.containsBean("programaExchange")).isTrue();
            assertThat(context.containsBean("programaEventExchange")).isTrue();
            assertThat(context.containsBean("nucleoRelacionamentoExchange")).isTrue();

            TopicExchange acolhimentoExchange = context.getBean("acolhimentoExchange", TopicExchange.class);
            TopicExchange acolhimentoEventExchange = context.getBean("acolhimentoEventExchange", TopicExchange.class);
            TopicExchange programaExchange = context.getBean("programaExchange", TopicExchange.class);
            TopicExchange programaEventExchange = context.getBean("programaEventExchange", TopicExchange.class);
            TopicExchange nucleoRelacionamentoExchange = context.getBean("nucleoRelacionamentoExchange",
                    TopicExchange.class);
            assertThat(acolhimentoExchange.getName()).isEqualTo(ExchangeCatalog.ACOLHIMENTO_COMMAND);
            assertThat(acolhimentoEventExchange.getName()).isEqualTo(ExchangeCatalog.ACOLHIMENTO_EVENT);
            assertThat(programaExchange.getName()).isEqualTo(ExchangeCatalog.PROGRAMA_COMMAND);
            assertThat(programaEventExchange.getName()).isEqualTo(ExchangeCatalog.PROGRAMA_EVENT);
            assertThat(nucleoRelacionamentoExchange.getName()).isEqualTo(ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT);

            assertThat(context.containsBean("nucleoRelacionamentoAcolhimentoDlq")).isTrue();
            assertThat(context.containsBean("nucleoRelacionamentoAcolhimentoQueue")).isTrue();
            assertThat(context.containsBean("nucleoRelacionamentoProgramaDlq")).isTrue();
            assertThat(context.containsBean("nucleoRelacionamentoProgramaQueue")).isTrue();

            Queue acolhimentoQueue = context.getBean("nucleoRelacionamentoAcolhimentoQueue", Queue.class);
            Queue programaQueue = context.getBean("nucleoRelacionamentoProgramaQueue", Queue.class);
            assertThat(acolhimentoQueue.getArguments()).containsEntry("x-delivery-limit", 3);
            assertThat(programaQueue.getArguments()).containsEntry("x-delivery-limit", 3);

            assertThat(context.containsBean("bindAcolhimentoCreated")).isTrue();
            assertThat(context.containsBean("bindAcolhimentoUpdated")).isTrue();
            assertThat(context.containsBean("bindAcolhimentoDeleted")).isTrue();
            assertThat(context.containsBean("bindProgramaCreated")).isTrue();
            assertThat(context.containsBean("bindProgramaUpdated")).isTrue();
            assertThat(context.containsBean("bindProgramaDeleted")).isTrue();
        }
    }
}
