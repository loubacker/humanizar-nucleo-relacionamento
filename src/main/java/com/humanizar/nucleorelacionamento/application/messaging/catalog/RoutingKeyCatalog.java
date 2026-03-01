package com.humanizar.nucleorelacionamento.application.messaging.catalog;

public final class RoutingKeyCatalog {

    // Inbound — acolhimento
    public static final String ACOLHIMENTO_CREATED_V1 = "ev.acolhimento.created.v1";
    public static final String ACOLHIMENTO_UPDATED_V1 = "ev.acolhimento.updated.v1";
    public static final String ACOLHIMENTO_DELETED_V1 = "ev.acolhimento.deleted.v1";

    // Inbound — programa
    public static final String PROGRAMA_CREATED_V1 = "ev.programa.created.v1";
    public static final String PROGRAMA_UPDATED_V1 = "ev.programa.updated.v1";
    public static final String PROGRAMA_DELETED_V1 = "ev.programa.deleted.v1";

    // Outbound — nucleo-relacionamento
    public static final String RESPONSAVEL_VINCULADO_V1 = "ev.nucleo.responsavel.vinculado.v1";
    public static final String RESPONSAVEL_DESVINCULADO_V1 = "ev.nucleo.responsavel.desvinculado.v1";
    public static final String ACOLHIMENTO_PROCESSED_V1 = "ev.nucleo-relacionamento.acolhimento.processed.v1";
    public static final String ACOLHIMENTO_REJECTED_V1 = "ev.nucleo-relacionamento.acolhimento.rejected.v1";
    public static final String PROGRAMA_PROCESSED_V1 = "ev.nucleo-relacionamento.programa.processed.v1";
    public static final String PROGRAMA_REJECTED_V1 = "ev.nucleo-relacionamento.programa.rejected.v1";

    public static boolean isAcolhimentoInbound(String routingKey) {
        return ACOLHIMENTO_CREATED_V1.equals(routingKey)
                || ACOLHIMENTO_UPDATED_V1.equals(routingKey)
                || ACOLHIMENTO_DELETED_V1.equals(routingKey);
    }

    public static boolean isProgramaInbound(String routingKey) {
        return PROGRAMA_CREATED_V1.equals(routingKey)
                || PROGRAMA_UPDATED_V1.equals(routingKey)
                || PROGRAMA_DELETED_V1.equals(routingKey);
    }

    private RoutingKeyCatalog() {
    }
}
