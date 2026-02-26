package com.humanizar.nucleorelacionamento.domain.model.enums;

public enum ReasonCode {

    HAS_ABORDAGEM(409, "Nao e permitido remover nucleo com abordagem vinculada.", false),
    RESPONSAVEL_REQUIRED(422, "NucleoPatient exige ao menos um responsavel.", false),
    NUCLEO_PATIENT_NOT_FOUND(404, "NucleoPatient nao encontrado.", false),
    ABORDAGEM_DUPLICATED(409, "Abordagem ja vinculada para este nucleoPatient.", false),
    VALIDATION_ERROR(400, "Falha de validacao do payload/evento.", false),
    UNSUPPORTED_EVENT_VERSION(422, "Versao de evento nao suportada.", false),
    UNSUPPORTED_ROUTING_KEY(400, "Routing key nao suportada.", false),
    DUPLICATE_EVENT(409, "Evento duplicado ja processado.", false),
    INTEGRATION_FAILURE(502, "Falha de integracao com dependencia externa.", true),
    PERSISTENCE_FAILURE(503, "Falha de persistencia no banco de dados.", true);

    private final int statusCode;
    private final String message;
    private final boolean retryable;

    ReasonCode(int statusCode, String message, boolean retryable) {
        this.statusCode = statusCode;
        this.message = message;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
