package com.humanizar.nucleorelacionamento.infrastructure.config.rabbit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

class RabbitTopologySplitConfigTest {

    @Test
    void shouldDeclareExchangesQueuesAndBindingsWithConfiguredDeliveryLimit() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context, "app.messaging.inbound.delivery-limit=3");

            context.register(RabbitExchangeConfig.class, RabbitQueueConfig.class, RabbitBindingConfig.class);
            context.refresh();

            assertThat(context.containsBean("acolhimentoExchange")).isTrue();
            assertThat(context.containsBean("programaExchange")).isTrue();
            assertThat(context.containsBean("nucleoRelacionamentoExchange")).isTrue();

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
