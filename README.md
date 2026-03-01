<div align="center">
  <h1>Humanizar - Núcleo Relacionamento (Microservice)</h1>
  <p>Gestão do Relacionamento entre Núcleo e Paciente dentro do ecossistema Humanizar.</p>

  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img alt="Docker" src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</div>

<br/>

O microserviço controla as vinculações de pacientes aos núcleos de atendimento, gerencia os responsáveis (Coordenadores/Administradores) e mantém a sincronia de abordagens clínicas através de uma arquitetura estritamente **Event-Driven**. Não há endpoints REST expostos; toda a interação ocorre através do consumo e produção de mensagens no RabbitMQ.

## Arquitetura e Padrões

O projeto foi construído focando em alta escalabilidade, resiliência e isolamento de regras de negócio.

- **Hexagonal Architecture (Ports & Adapters):** O core da aplicação (`domain`) é isolado de tecnologias externas. A comunicação com o banco de dados e mensageria é feita através de portas (interfaces) e adaptadores (`infrastructure`).
- **Transactional Outbox Pattern:** Para garantir a entrega de mensagens ao RabbitMQ sem risco de dupla escrita ou perda de dados, os eventos de saída são salvos na mesma transação de banco (`outbox_event`) e despachados assincronamente por um Relay Worker (`OutboxRelayWorker`).
- **Idempotência:** Todo evento de entrada possui um mecanismo de guarda (`ProcessedEventGuard`) que registra eventos processados na tabela `processed_event`. Eventos duplicados são ignorados automaticamente.
- **Virtual Threads:** Otimização de concorrência e I/O bloqueante habilitada ativamente no Spring Boot (`VirtualThreadsConfig`), utilizando o poder do Project Loom do Java 25 para consumo massivo de filas.

## Contexto de Negócio e Domínio

A base da aplicação é regida por 3 entidades principais do domínio:

- **`NucleoPatient`:** A entidade agregadora central. Define a qual Núcleo de Atendimento um Paciente pertence, baseando-se nos eventos de acolhimento e programas recebidos.
- **`NucleoPatientResponsavel`:** Gerencia as permissões e o ciclo de vida dos profissionais (Coordenadores e Administradores) responsáveis pelos núcleos e pacientes vinculados.
- **`AbordagemPatient`:** Mantém o histórico e a sincronização das abordagens terapêuticas aplicadas ao paciente durante o programa de atendimento.

## 🔄 Comunicação Assíncrona (RabbitMQ)

A aplicação atua como um **Worker Service**, sendo reativa a eventos externos para consolidar seu domínio e emitindo eventos como consequência.

### Consome (Inbound)

A aplicação escuta filas para sincronizar o relacionamento quando ações acontecem nos microserviços de Acolhimento e Programa:

**Exchange: `humanizar.acolhimento.event`**
- `ev.acolhimento.created.v1`
- `ev.acolhimento.updated.v1`
- `ev.acolhimento.deleted.v1`

**Exchange: `humanizar.programa.event`**
- `ev.programa.created.v1`
- `ev.programa.updated.v1`
- `ev.programa.deleted.v1`

### Produz (Outbound - Via Outbox)

Ao processar eventos com sucesso (ou falha impeditiva), a aplicação emite resultados de volta ao ecossistema usando a tabela de Outbox:

**Exchange: `humanizar.nucleo-relacionamento.event`**
- `ev.nucleo.responsavel.vinculado.v1`
- `ev.nucleo.responsavel.desvinculado.v1`
- `ev.nucleo-relacionamento.[acolhimento|programa].processed.v1`
- `ev.nucleo-relacionamento.[acolhimento|programa].rejected.v1`

## Resiliência e Tolerância a Falhas

Como a saúde do ecossistema depende deste Worker, estratégias de resiliência foram implementadas na camada de `infrastructure`:

- **Dead Letter Queues (DLQ):** Mensagens não processadas por falhas sistêmicas nas filas principais caem em filas `-dlq` e são reprocessadas de forma controlada ou alertadas via `DeadLetterConsumer`.
- **Outbox Retry Policy:** O despachante de eventos (Outbox) utiliza uma política de retentativas. Se o RabbitMQ estiver indisponível no momento de publicar uma mensagem salva no banco, a `OutboxRetryPolicy` garante tentativas graduais (backoff) antes de marcá-la como `FAILED`.
- **Validação Antecipada:** O `EnvelopeValidator` e os validadores de payload bloqueiam mensagens malformadas antes mesmo de tocarem a regra de negócio.

## Estrutura do Projeto

```text
src/main/java/com/humanizar/nucleorelacionamento/
├── application/      # Casos de uso, DTOs, Mappers e Orquestração (Inbound/Outbound)
├── domain/           # Regras de negócio, Entidades (Entities), Enums, Exceções e Ports
└── infrastructure/   # Adapters (JPA, RabbitMQ, Outbox), Configurações, Consumidores
```

## 🔐 Segurança

Mesmo sem rotas HTTP públicas para clientes, as proteções e configurações Spring Security via OAuth2 Resource Server utilizando JWT estão prontas. A validação das claims é extraída do token JWT gerado pelo Auth Server (definido em `AUTH_SERVER_URL`), para uso futuro ou chamadas S2S (Server-to-Server).

## Como Executar Localmente

### Pré-requisitos
- JDK 25
- Docker e Docker Compose (para banco de dados, mensageria e Redis)
- Maven 3.9+

### Configuração de Variáveis de Ambiente
Crie um arquivo `.env` na raiz do projeto (o Spring fará a leitura automática) com base no `application.yaml`:

```env
DB_URL=jdbc:postgresql://localhost:5432/db
DB_USERNAME=postgres
DB_PASSWORD=secret
RABBITMQ_URL=amqp://guest:guest@localhost:5672
AUTH_SERVER_URL=http://localhost:8080
```

### Executando a Aplicação
Suba os serviços dependentes (PostgreSQL, RabbitMQ, Redis) utilizando um `docker-compose.yml`. Em seguida, instale as dependências e rode via Maven Wrapper:

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

A aplicação iniciará na porta `9001` (padrão). O Health Check (Actuator) pode ser acessado em http://localhost:9001/actuator/health.

## 🛠️ Workers em Background

O sistema possui schedulers essenciais rodando em background (`@EnableScheduling`):
- **OutboxRelayWorker (5s):** Busca eventos na tabela de outbox com status `NEW` ou `FAILED` e publica no RabbitMQ.
- **RetentionWorker (1h):** Realiza a limpeza de eventos antigos (Purge Cleanup):
  - Eventos de Outbox (`PUBLISHED`, `DEAD`) mais velhos que 48 horas.
  - Eventos Processados (Idempotência) mais velhos que 90 dias.

## 🐳 Docker

A aplicação conta com um Dockerfile otimizado em Multi-Stage Build (Alpine Linux) utilizando `eclipse-temurin:25-jre-alpine`. Decisões arquiteturais tomadas para orquestração (Kubernetes/ECS):

- Usuário `appuser` sem privilégios de root (Security First).
- Variáveis de ambiente OOTB (Out of The Box): `MALLOC_ARENA_MAX=2` para reduzir fragmentação de memória, além de travas no GC (`-XX:+UseSerialGC`) e Metaspace para focar o processamento nas Virtual Threads através do `JAVA_TOOL_OPTIONS`.
