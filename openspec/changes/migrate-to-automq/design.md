## Context

A aplicação base atual (`system-async-kafka`) é dependente de Apache Kafka tradicional configurado localmente. Estamos migrando a cópia deste sistema (`system-async-kafka-migrate-automq`) para adotar o broker **AutoMQ**, um clone do Kafka otimizado para Cloud-Native (S3-backed), visando reduzir o TCO e a complexidade operacional da mensageria local e facilitar a configuração de multi-cloud.

## Goals / Non-Goals

**Goals:**
- Desvincular as referências e dependências diretas de nomenclatura do projeto antigo (`system-async-kafka`).
- Reconfigurar o projeto para usar a infraestrutura do AutoMQ de forma nativa e agnóstica via propriedades Spring.
- Oferecer uma configuração de Docker Compose para subir um ambiente local do AutoMQ (em conjunto com MinIO para storage S3), simulando as capacidades do ambiente de produção.

**Non-Goals:**
- Mudar ou refatorar o código Java / lógica de negócio, visto que a compatibilidade com a API do Kafka é total (drop-in replacement).

## Decisions

- **Drop-in Configuration**: Iremos usar a propriedade nativa `spring.kafka.bootstrap-servers` nos properties do Spring para apontar ao AutoMQ. Isso centraliza a alteração apenas na camada de configuração.
- **Docker Compose com MinIO local**: Para não depender de cloud pública durante desenvolvimento, utilizaremos MinIO como substituto do S3 na orquestração local, exatamente como definido na spec de arquitetura do CRM.
- **Renomeação nos arquivos de build**: O `settings.gradle` precisa obrigatoriamente refletir a nova estrutura da pasta (`system-async-kafka-migrate-automq`) para o correto carregamento do projeto e evitar conflitos.

## Risks / Trade-offs

- **Risk: Instabilidade na execução de testes com AutoMQ local versus Embedded Kafka** → Mitigation: Manteremos o ambiente de teste o mais próximo do de dev, com a adoção do Docker Compose documentado, enquanto avaliamos se os testes via `Testcontainers` precisam apontar para uma imagem compatível com AutoMQ futuramente.
