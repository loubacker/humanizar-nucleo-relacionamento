package com.humanizar.nucleorelacionamento.application.messaging.catalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExchangeCatalogTest {

    @Test
    void shouldExposeExpectedExchangeLiterals() {
        Assertions.assertEquals("humanizar.acolhimento.command", ExchangeCatalog.ACOLHIMENTO_COMMAND);
        Assertions.assertEquals("humanizar.acolhimento.event", ExchangeCatalog.ACOLHIMENTO_EVENT);
        Assertions.assertEquals("humanizar.programa.command", ExchangeCatalog.PROGRAMA_COMMAND);
        Assertions.assertEquals("humanizar.nucleo-relacionamento.event", ExchangeCatalog.NUCLEO_RELACIONAMENTO_EVENT);
    }
}
