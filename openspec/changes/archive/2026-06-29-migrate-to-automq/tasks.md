## 1. Project Renaming

- [x] 1.1 Atualizar `rootProject.name` em `settings.gradle` para `system-async-kafka-migrate-automq`
- [x] 1.2 Atualizar `spring.application.name` em `src/main/resources/application.properties` para `system-async-kafka-migrate-automq`
- [x] 1.3 Atualizar `spring.application.name` em `src/main/resources/application-test.properties` para `system-async-kafka-migrate-automq`

## 2. AutoMQ Configuration

- [x] 2.1 Criar arquivo `docker-compose.yml` na raiz do projeto contendo os containers do AutoMQ e do MinIO (se necessário) para ambiente local.
- [x] 2.2 Alterar a propriedade `spring.kafka.bootstrap-servers` no `src/main/resources/application-test.properties` para apontar para o endpoint do AutoMQ.
- [x] 2.3 Executar build do projeto via Gradle para validar que a renomeação não quebrou a compilação.
- [x] 2.4 Subir a stack do `docker-compose.yml` e executar a aplicação para validar a conexão drop-in com o AutoMQ.
