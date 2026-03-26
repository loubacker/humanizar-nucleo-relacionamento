<div align="center">
  <h1>Humanizar - Núcleo de Relacionamento (Microservice)</h1>
  <p>Gestão do relacionamento entre Núcleo e Paciente no ecossistema Humanizar.</p>

  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.0.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img alt="GraalVM" src="https://img.shields.io/badge/GraalVM_Native-25-E76F00?style=for-the-badge&logo=oracle&logoColor=white" />
  <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
</div>

<br/>

Serviço EDA (sem endpoint REST de negócio), responsável por sincronizar vínculos de núcleos, responsáveis e abordagens a partir de eventos inbound, com idempotência, outbox transacional e callbacks de processamento.

## Arquitetura e Padrões

- Arquitetura Hexagonal (`application`, `domain`, `infrastructure`).
- Inbound segregado por routing key (handlers dedicados por operação).
- Validação de envelope/payload em mappers inbound por domínio/operação.
- Outbox transacional para publicação assíncrona confiável.
- Idempotência via `processed_event` antes do processamento da regra.
- `ACK/NACK` manual do RabbitMQ com política explícita por tipo de erro.
- Runtime opcional em binário nativo com GraalVM Native Image.

## 🔄 Comunicação Assíncrona (RabbitMQ)

## Inbound

**Exchange `humanizar.acolhimento.command`**
- `cmd.acolhimento.created.v1`
- `cmd.acolhimento.updated.v1`
- `cmd.acolhimento.deleted.v1`

**Exchange `humanizar.programa.command`**
- `cmd.programa.created.v1`
- `cmd.programa.updated.v1`
- `cmd.programa.deleted.v1`

## Outbound

O outbound está separado em duas trilhas:

1. **Eventos de domínio downstream**

**Exchange `humanizar.nucleo-relacionamento.event`**
- `ev.nucleo.responsavel.vinculado.v1`
- `ev.nucleo.responsavel.desvinculado.v1`

Contrato publicado: `OutboundEnvelopeDTO<T>` (metadados + payload).

2. **Callbacks de processamento para upstream**

**Exchange `humanizar.acolhimento.event`**
- `ev.acolhimento.nucleo-relacionamento.processed.v1`
- `ev.acolhimento.nucleo-relacionamento.rejected.v1`

**Exchange `humanizar.programa.event`**
- `ev.programa.nucleo-relacionamento.processed.v1`
- `ev.programa.nucleo-relacionamento.rejected.v1`

Contrato publicado: `CallbackDTO`.

## ⛓️‍💥 Resiliência e tolerância a falhas

### ACK/NACK manual

`rabbitListenerContainerFactory` roda com `AcknowledgeMode.MANUAL`.

Política de consumo:
- `ack`: sucesso, duplicidade ou erro funcional sem retry.
- `nackRetry` (`requeue=true`): o erro segue para retry.
- `nackDeadLetter` (`requeue=false`): parse inválido / mensagem inválida.

Implementação central: `RabbitAcknowledgementConfig`.

### DLQ

Filas principais:
- `humanizar.nucleo-relacionamento.acolhimento`
- `humanizar.nucleo-relacionamento.programa`

Filas de dead letter:
- `humanizar.nucleo-relacionamento.acolhimento.dlq`
- `humanizar.nucleo-relacionamento.programa.dlq`

### Idempotência

Antes do processamento da regra, o consumer valida duplicidade por meio do `ProcessedEventGuard`.

## 🛠️ Workers em Background

### OutboxRelayWorker (5s)

- Faz claim de eventos em `NEW`, `FAILED` e `LOCKED`.
- Aplica lock ownership por `instanceId` (fencing).
- Publica de forma assíncrona com controle de paralelismo.

### OutboxEventProcessor

Transações de status:
- sucesso: `LOCKED -> PUBLISHED`
- falha com retry: `LOCKED -> FAILED` (com `nextRetryAt`)
- tentativas esgotadas: `LOCKED -> DEAD`

### RetentionWorker (1h)

- Remove outbox antigo (`PUBLISHED` e `DEAD`) com mais de 48h.
- Remove `processed_event` antigo com mais de 90 dias.

## Estrutura do Projeto

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

### Pré-requisitos
- JDK 25
- Maven 3.9+
- PostgreSQL
- RabbitMQ

### Variáveis de Ambiente (`.env`)

```env
DB_URL=jdbc:postgresql://localhost:5432/db
DB_USERNAME=postgres
DB_PASSWORD=secret
RABBITMQ_URL=amqp://guest:guest@localhost:5672
AUTH_SERVER_URL=http://localhost:8080
```

### Execução local (JVM)

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

Porta padrao: `9001`.
Health check: `http://localhost:9001/actuator/health`.

## 🐳 Docker Native (GraalVM)

O Dockerfile do módulo usa build multi-stage com GraalVM Native Image:

1. Build stage (`ghcr.io/graalvm/native-image-community:25`) compila com:
   - `./mvnw -Pnative -DskipTests native:compile`
2. Runtime stage (`debian:bookworm-slim`) executa binario nativo:
   - `/app/app-binario`

Exemplo:

```bash
docker build -t humanizar-nucleo-relacionamento:native .
docker run --rm -p 9001:9001 --env-file .env humanizar-nucleo-relacionamento:native
```
