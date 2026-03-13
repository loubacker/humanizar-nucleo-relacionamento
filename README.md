<div align="center">
  <h1>Humanizar - Nucleo Relacionamento (Microservice)</h1>
  <p>Gestao do relacionamento entre Nucleo e Paciente no ecossistema Humanizar.</p>

  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
</div>

<br/>

Servico EDA (sem endpoint REST de negocio) responsavel por sincronizar vinculos de nucleos, responsaveis e abordagens a partir de eventos inbound, com idempotencia, outbox transacional e callbacks de processamento.

## Arquitetura e padroes

- Hexagonal architecture (`application`, `domain`, `infrastructure`).
- Inbound segregado por routing key (handlers dedicados por operacao).
- Validacao de envelope/payload em mappers inbound por dominio/operacao.
- Outbox transacional para publicacao assincrona confiavel.
- Idempotencia via `processed_event` antes do processamento de regra.
- ACK/NACK manual do RabbitMQ com politica explicita por tipo de erro.

## Comunicacao assincrona (RabbitMQ)

### Inbound (consome)

**Exchange `humanizar.acolhimento.command`**
- `cmd.acolhimento.created.v1`
- `cmd.acolhimento.updated.v1`
- `cmd.acolhimento.deleted.v1`

**Exchange `humanizar.programa.command`**
- `cmd.programa.created.v1`
- `cmd.programa.updated.v1`
- `cmd.programa.deleted.v1`

### Outbound (produz via outbox)

O outbound esta separada em 2 trilhas:

1. **Eventos de dominio downstream**

**Exchange `humanizar.nucleo-relacionamento.event`**
- `ev.nucleo.responsavel.vinculado.v1`
- `ev.nucleo.responsavel.desvinculado.v1`

Contrato publicado: `OutboundEnvelopeDTO<T>` (metadados + payload).

2. **Callbacks de processamento para upstream**

**Exchange `humanizar.acolhimento.event`**
- `ev.acolhimento.nucleo-relacionamento.processed.v1`
- `ev.acolhimento.nucleo-relacionamento.rejected.v1`

**Exchange `humanizar.programa.event`**
- `ev.nucleo-relacionamento.programa.processed.v1`
- `ev.nucleo-relacionamento.programa.rejected.v1`

Contrato publicado: `CallbackDTO`.

## Contratos de payload outbound

### 1) Dominio Downstream (`humanizar.nucleo-relacionamento.event`)

```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "producerService": "humanizar-nucleo-relacionamento",
  "occurredAt": "2026-03-13T02:00:00",
  "actorId": "uuid",
  "userAgent": "Mozilla/5.0 ...",
  "originIp": "127.0.0.1",
  "payload": {
    "patientId": "uuid",
    "nucleoPatient": []
  }
}
```

### 2) Callback Upstream (`humanizar.acolhimento.event` / `humanizar.programa.event`)

```json
{
  "upStream": "cmd.acolhimento.created.v1",
  "eventId": "uuid",
  "correlationId": "uuid",
  "producerService": "humanizar-nucleo-relacionamento",
  "exchangeName": "humanizar.acolhimento.event",
  "routingKey": "ev.acolhimento.nucleo-relacionamento.processed.v1",
  "aggregateType": "acolhimento",
  "aggregateId": "uuid",
  "eventVersion": 1,
  "occurredAt": "2026-03-13T02:00:00",
  "actorId": "uuid",
  "userAgent": "Mozilla/5.0 ...",
  "originIp": "127.0.0.1",
  "status": "PROCESSED",
  "reasonCode": null,
  "errorMessage": null,
  "processedAt": "2026-03-13T02:00:00",
  "rejectedAt": null
}
```

## Resiliencia e tolerancia a falhas

### ACK/NACK manual

`rabbitListenerContainerFactory` roda com `AcknowledgeMode.MANUAL`.

Política de consumo:
- `ack`: sucesso, duplicado, erro funcional sem retry.
- `nackRetry` (`requeue=true`): erro segue para retry.
- `nackDeadLetter` (`requeue=false`): parse invalido / mensagem invalida.

Implementacao central: `RabbitAcknowledgementConfig`.

### DLQ

Filas principais:
- `humanizar-nucleo-relacionamento.acolhimento`
- `humanizar-nucleo-relacionamento.programa`

Filas de dead-letter:
- `humanizar-nucleo-relacionamento.acolhimento.dlq`
- `humanizar-nucleo-relacionamento.programa.dlq`

### Idempotencia

Antes do processamento de regra, o consumer valida duplicidade via `ProcessedEventGuard`.

## Workers em background

### OutboxRelayWorker (5s)

- Faz claim de eventos em `NEW`, `FAILED` e `LOCKED`.
- Aplica lock ownership por `instanceId` (fencing).
- Publica assincronamente com controle de paralelismo.

### OutboxEventProcessor

Transicoes de status:
- sucesso: `LOCKED -> PUBLISHED`
- falha retentavel: `LOCKED -> FAILED` (com `nextRetryAt`)
- tentativas esgotadas: `LOCKED -> DEAD`

### RetentionWorker (1h)

- Remove outbox antigo (`PUBLISHED` e `DEAD`) > 48h.
- Remove `processed_event` antigo > 90 dias.

## Estrutura do projeto

```text
src/main/java/com/humanizar/nucleorelacionamento/
|-- application/
|   |-- messaging/inbound/handler/   # handlers por routing key (acolhimento/programa)
|   |-- messaging/inbound/mapper/    # EnvelopeInboundMapper + mappers por operacao
|   |-- messaging/outbound/dto/      # CallbackDTO + OutboundEnvelopeDTO + payload DTOs
|   |-- messaging/outbound/mapper/   # OutboxEventMapper + mappers outbound de dominio/callback
|-- domain/                          # entidades, enums, exceptions, ports
`-- infrastructure/                  # adapters, rabbit config, consumers, outbox relay
```

## Como executar localmente

### Pre-requisitos
- JDK 25
- Maven 3.9+
- PostgreSQL e RabbitMQ (ex.: Docker Compose)

### Variaveis de ambiente

Crie `.env` na raiz do modulo:

```env
DB_URL=jdbc:postgresql://localhost:5432/db
DB_USERNAME=postgres
DB_PASSWORD=secret
RABBITMQ_URL=amqp://guest:guest@localhost:5672
AUTH_SERVER_URL=http://localhost:8080
```

### Execucao

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

Porta padrao: `9001`.
Health check: `http://localhost:9001/actuator/health`.
