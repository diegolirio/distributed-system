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

### Arquitetura de Armazenamento (Local vs. Produção)

Como o AutoMQ é *stateless* e delega a persistência, ele requer o uso de um *Object Storage* por baixo dos panos para guardar as mensagens. Para o **ambiente local**, a prática recomendada é utilizar o **MinIO**, que atua como um simulador da API do Amazon S3 rodando em um container Docker na máquina do desenvolvedor.

Visualmente, a arquitetura local comparada à de produção fica assim:

```text
       AMBIENTE LOCAL (Docker)                 PRODUÇÃO (Cloud)
 ┌────────────────────────────────┐     ┌────────────────────────────────┐
 │        Aplicação Spring        │     │        Aplicação Spring        │
 └───────────────┬────────────────┘     └───────────────┬────────────────┘
                 │                                      │
 ┌───────────────▼────────────────┐     ┌───────────────▼────────────────┐
 │     AutoMQ (Broker Node)       │     │     AutoMQ (Broker Node)       │
 └───────────────┬────────────────┘     └───────────────┬────────────────┘
                 │ S3 API                               │ S3 API
 ┌───────────────▼────────────────┐     ┌───────────────▼────────────────┐
 │       MinIO (Container)        │     │       AWS S3 (Bucket)          │
 │   (Armazenamento em volume)    │     │   (Armazenamento gerenciado)   │
 └────────────────────────────────┘     └────────────────────────────────┘
```

**Implicações para o dev local:**
No `docker-compose.yml`, precisaremos de dois serviços rodando em conjunto:
1. **MinIO**: Subindo primeiro e criando um bucket localmente (ex: `automq-data`).
2. **AutoMQ**: Subindo em seguida, configurado para apontar para o container do MinIO (`http://minio:9000`).

*Ponto de Atenção:* Subir o AutoMQ + MinIO no Docker localmente pode consumir um pouco mais de memória RAM em comparação com um Kafka standalone.

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
