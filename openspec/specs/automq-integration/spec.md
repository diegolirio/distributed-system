# Capability: automq-integration

## Purpose
TBD (Migrate to AutoMQ)

## Requirements

### Requirement: Integração e Conexão via AutoMQ
A aplicação SHALL se conectar nativamente no endpoint do AutoMQ provido via variáveis de ambiente/properties sem necessidade de refatoração do código fonte (producer/consumer).

#### Scenario: Subida da aplicação conectando no Broker AutoMQ
- **WHEN** a aplicação inicializa no perfil `test` ou `default`
- **THEN** ela deve estabelecer conexão com sucesso via `spring.kafka.bootstrap-servers` usando o endereço especificado do AutoMQ.

### Requirement: Renomeação e Identidade do Projeto
A aplicação SHALL ser renomeada adequadamente nos seus artefatos de configuração para ser identificada isoladamente do sistema de onde foi copiada, evitando conflitos de build e instrumentação de métricas (`spring.application.name`).

#### Scenario: Build e inicialização do contexto
- **WHEN** executarmos a compilação do Gradle e subirmos a aplicação
- **THEN** o nome registrado no contexto do Spring (`spring.application.name`) e no Root Project (`settings.gradle`) deve ser obrigatoriamente `system-async-kafka-migrate-automq`.
