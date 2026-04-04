package com.humanizar.nucleorelacionamento.domain.model.enums;

public enum ReasonCode {

    PATIENT_NOT_FOUND(404, "Paciente não encontrado.", false),
    HAS_ABORDAGEM(409, "Não é permitido remover núcleo com abordagem vinculada.", false),
    RESPONSAVEL_REQUIRED(422, "NucleoPatient exige ao menos um responsavel.", false),
    NUCLEO_PATIENT_NOT_FOUND(404, "NucleoPatient não encontrado.", false),
    ABORDAGEM_DUPLICATED(409, "Abordagem já vinculada para este núcleoPatient.", false),
    VALIDATION_ERROR(400, "Falha de validação do payload/evento.", false),
    INBOUND_REQUIRED_FIELD(400, "Campo obrigatório ausente no inbound.", false),
    INBOUND_INVALID_ENUM(400, "Valor enum inválido no inbound.", false),
    INBOUND_EMPTY_COLLECTION(400, "Coleção obrigatória vazia no inbound.", false),
    INBOUND_DUPLICATE_ITEM(400, "Item duplicado encontrado no inbound.", false),
    INBOUND_PARSE_ERROR(400, "Falha ao parsear mensagem inbound.", false),
    UNSUPPORTED_EVENT_VERSION(422, "Versão de evento não suportada.", false),
    UNSUPPORTED_ROUTING_KEY(400, "Routing key não suportada.", false),
    DUPLICATE_EVENT(409, "Evento duplicado já processado.", false),
    INTEGRATION_FAILURE(502, "Falha de integração com dependência externa.", true),
    PERSISTENCE_FAILURE(503, "Falha de persistência no banco de dados.", true);

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
