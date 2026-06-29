# Research: Migração de Kafka para AutoMQ

## Contexto

A plataforma atual (`system-async-kafka-migrate-automq` baseada no `system-async-kafka`) utiliza Apache Kafka tradicional como broker de mensageria. A operação de clusters Kafka tradicionais possui um alto custo operacional (TCO) e de infraestrutura.

Esta pesquisa avalia a adoção do **AutoMQ** como broker substituto, com o objetivo de reduzir custos e facilitar a operação, mantendo a compatibilidade total com o protocolo Kafka.

## Por que usar o AutoMQ?

AutoMQ é uma alternativa de mensageria Cloud-Native que armazena dados primariamente em Object Storage (como AWS S3 ou MinIO). Ele remove a dependência de discos locais caros e de estado nos brokers (stateless brokers).

### Motivos principais:
1. **Zero Alteração de Código (Drop-in Replacement):** Suporta 100% do protocolo do Apache Kafka.
2. **Separação Computação/Armazenamento:** Permite escalar os nós de processamento sem rebalanceamento de partições.
3. **Custo-Benefício:** Redução drástica nos custos devido ao uso de Object Storage.

---

## Prós e Contras

### Prós (Vantagens) ✅
* **Total Compatibilidade:** APIs Kafka funcionam sem alteração.
* **Redução de Custos:** Object Storage é exponencialmente mais barato que EBS.
* **Escalabilidade Elástica:** Brokers stateless.
* **Sem Lock-in:** Funciona na AWS, GCP, Azure ou on-premise com MinIO.
* **Portabilidade:** Os dados ficam no seu bucket.

### Contras (Desafios) ❌
* **BYOC:** A gestão do storage e containers é responsabilidade do time.
* **Latência em P99:** Pode ter leve adição de ms em relação a discos NVMe locais.
* **Comunidade:** É uma tecnologia mais recente comparada ao padrão de mercado de 10 anos do Kafka.

## Conclusão da Pesquisa

O uso do **AutoMQ** é altamente recomendado, pois não exige refatoração de código e reduz custos significativamente.

**Próximos Passos:** Prosseguir com a criação da especificação para implementação.
