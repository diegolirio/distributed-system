## Why

A plataforma clone (`system-async-kafka-migrate-automq`) herdou as configurações e o nome do projeto original (`system-async-kafka`). Para que este novo projeto possa ser utilizado de forma independente e conecte-se ao AutoMQ (conforme pesquisa e plano de migração do CRM), precisamos renomear as referências internas do projeto e reconfigurar os apontamentos do Kafka. Isso resolve o problema de identidade do projeto e concretiza a mudança de infraestrutura para um broker mais econômico.

## What Changes

- Renomeação do projeto de `system-async-kafka` para `system-async-kafka-migrate-automq` nos arquivos de configuração do Gradle (`settings.gradle`) e do Spring (`application.properties`, `application-test.properties`).
- Alteração da configuração `spring.kafka.bootstrap-servers` para apontar para a infraestrutura do AutoMQ.
- Adição de um arquivo `docker-compose.yml` local para subir a infraestrutura do AutoMQ (Broker integrado a um MinIO local) para ambiente de desenvolvimento.

## Capabilities

### New Capabilities
- `automq-integration`: Integração da aplicação com a infraestrutura local do AutoMQ via Docker Compose e ajuste das variáveis de ambiente de conexão do Spring Kafka.

### Modified Capabilities

## Impact

- **Código Fonte**: Nenhuma classe Java será afetada, garantindo total compatibilidade (Drop-in replacement).
- **Configuração**: Alteração no nome da aplicação no Spring e no rootProject do Gradle.
- **Ambiente de Desenvolvimento**: O time passa a necessitar do `docker-compose.yml` atualizado para testes locais.
