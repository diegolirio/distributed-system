# Migração do Kafka para o Redpanda (Self-hosted)

## Por que Redpanda?
Em uma análise técnica para simplificar a infraestrutura, diminuir o custo de operação e aumentar a performance, o **Redpanda** destacou-se como o melhor candidato para substituir o ecossistema atual do Apache Kafka.

Abaixo, os prós e contras relacionados à mudança e os motivos que levaram a essa decisão, em aderência à diretriz de **"nenhum contato com a infraestrutura atual do Kafka"**.

### Vantagens (Prós)

1. **Compatibilidade com a API do Kafka (Kafka-API Compatible)**
   - O Redpanda suporta nativamente a API do Kafka. Isso significa que não foi necessário mudar *nenhuma linha* de código da aplicação no nível de negócio. 
   - Ferramentas como o `Spring Kafka`, bibliotecas clientes e ferramentas de ecossistema funcionam out-of-the-box.
   
2. **Desempenho e Arquitetura**
   - **Sem JVM / Escrito em C++:** Consome muito menos memória e CPU em comparação ao Kafka tradicional, que exige JVM.
   - **Thread per Core (Seastar framework):** Aproveita ao máximo o hardware moderno (NVMe), entregando latências de milissegundos num percentil (p99) muito menor que o Kafka.
   - **Sem ZooKeeper / KRaft:** O Redpanda adota nativamente o algoritmo de consenso Raft internamente, simplificando imensamente a topologia. Não há mais múltiplos serviços de cluster para gerenciar.

3. **Facilidade em Ambientes de Testes**
   - O setup com **Testcontainers** ficou consideravelmente mais leve e rápido (`RedpandaContainer`), subindo com muito menos atrito do que um stack completo Kafka/Zookeeper.

4. **Self-hosted e Cloud-agnostic**
   - Pode ser implantado em qualquer cloud provider (AWS, GCP, Azure) ou On-Premise usando a mesma imagem Docker/Binário. 
   - A licença **BSL 1.1** (Source-available) atende integralmente os requisitos para uso interno.

### Desvantagens (Contras)

1. **Assincronicidade Agressiva em Testes**
   - Como o Redpanda sobe incrivelmente rápido e inicia operações agressivas de IO, a coordenação com os testes de integração exige um pouco mais de cuidado. Foi necessário forçar esperas na atribuição de partições do Consumer e alterar a política para `auto-offset-reset=earliest` para evitar a perda de mensagens em ambientes efêmeros (testes).
   - O Spring Kafka 3.x expõe algumas peculiaridades com operações assíncronas de envio para tópicos de DLQ (`DeadLetterPublishingRecoverer`), o que forçou o uso de implementações síncronas de recuperação no teste de integração.

2. **Novos Paradigmas de Troubleshoot**
   - Embora suporte a API do Kafka, os logs do broker, os mecanismos de dump de segmento e o gerenciamento de recursos seguem uma filosofia diferente (baseada no SO e C++). As equipes de plataforma precisarão adaptar seus playbooks.

## O Que Foi Alterado

Para a prova de conceito e estabilização (no módulo `system-async-kafka-migrate-redpanda`):

1. **Infraestrutura Testcontainers:** Substituição do `KafkaContainer` pelo `RedpandaContainer`.
2. **Propriedades (application.properties):** Ajuste dos offsets para `earliest` para proteção no start assíncrono.
3. **KafkaConfig (DLQ):** Ajuste do `DefaultErrorHandler` para garantir o envio síncrono para a fila `.DLT` com sucesso antes do encerramento forçado do teste.
4. **Nomenclatura:** Atualização dos pacotes e imports para garantir que esse módulo está 100% livre da antiga infra de Kafka.

A migração foi bem-sucedida, com os testes fluindo rapidamente, isolamento garantido e as restrições arquiteturais da prova de conceito foram integralmente respeitadas.
