package com.humanizar.nucleorelacionamento.application.messaging.catalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoutingKeyCatalogTest {

    @Test
    void shouldExposeExpectedAcolhimentoCallbackRoutingKeyLiterals() {
        Assertions.assertEquals(
                "ev.acolhimento.nucleo-relacionamento.processed.v1",
                RoutingKeyCatalog.ACOLHIMENTO_PROCESSED_V1);
        Assertions.assertEquals(
                "ev.acolhimento.nucleo-relacionamento.rejected.v1",
                RoutingKeyCatalog.ACOLHIMENTO_REJECTED_V1);
    }

    @Test
    void shouldExposeExpectedProgramaRoutingKeyLiterals() {
        Assertions.assertEquals("cmd.programa.created.v1", RoutingKeyCatalog.PROGRAMA_CREATED_V1);
        Assertions.assertEquals("cmd.programa.updated.v1", RoutingKeyCatalog.PROGRAMA_UPDATED_V1);
        Assertions.assertEquals("cmd.programa.deleted.v1", RoutingKeyCatalog.PROGRAMA_DELETED_V1);
    }

    @Test
    void shouldIdentifyAcolhimentoInboundKeys() {
        Assertions.assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1));
        Assertions.assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1));
        Assertions.assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1));
        Assertions.assertFalse(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.PROGRAMA_CREATED_V1));
    }

    @Test
    void shouldIdentifyProgramaInboundKeys() {
        Assertions.assertTrue(RoutingKeyCatalog.isProgramaInbound(RoutingKeyCatalog.PROGRAMA_CREATED_V1));
        Assertions.assertTrue(RoutingKeyCatalog.isProgramaInbound(RoutingKeyCatalog.PROGRAMA_UPDATED_V1));
        Assertions.assertTrue(RoutingKeyCatalog.isProgramaInbound(RoutingKeyCatalog.PROGRAMA_DELETED_V1));
        Assertions.assertFalse(RoutingKeyCatalog.isProgramaInbound(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1));
    }
}
