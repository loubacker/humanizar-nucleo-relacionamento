package com.humanizar.nucleorelacionamento.application.messaging.catalog;

public final class QueueCatalog {

    public static final String NUCLEO_RELACIONAMENTO_ACOLHIMENTO = "humanizar-nucleo-relacionamento.acolhimento";
    public static final String NUCLEO_RELACIONAMENTO_PROGRAMA = "humanizar-nucleo-relacionamento.programa";

    public static final String NUCLEO_RELACIONAMENTO_ACOLHIMENTO_DLQ = "humanizar-nucleo-relacionamento.acolhimento.dlq";
    public static final String NUCLEO_RELACIONAMENTO_PROGRAMA_DLQ = "humanizar-nucleo-relacionamento.programa.dlq";

    private QueueCatalog() {
    }
}
