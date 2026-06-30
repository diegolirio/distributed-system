# Research: Alternativas ao Apache Kafka para o Ecossistema CRM

**Feature**: 001-kafka-broker-migration
**Date**: 2026-06-19 (atualizado com critérios multi-cloud e custo primário)
**Escopo**: 19 serviços Spring Kafka (Java 11), 30+ tópicos, Maven/Gradle

---

## Critérios de Eliminação (Gate Obrigatório)

Qualquer candidato que viole um destes critérios é **eliminado automaticamente**, independente de custo ou desempenho:

| Gate | Requisito | Origem |
|---|---|---|
| **G1 — Compatibilidade de código** | Spring Kafka deve funcionar sem alteração de código | FR-006 |
| **G2 — Consumer groups** | Suporte nativo a consumer groups independentes | FR-007 |
| **G3 — Multi-cloud** | Deve operar em ao menos 2 cloud providers distintos sem refatoração | FR-009 |
| **G4 — Portabilidade de dados** | Migração de provider não pode causar perda de mensagens | FR-010 |

**Critério de desempate**: Entre candidatos que passam todos os gates, o de **menor TCO mensal** vence (FR-008 + FR-010).

---

## Contexto da Pesquisa

Todos os serviços CRM utilizam **Spring Kafka** como cliente de mensageria. Isso é determinante na avaliação: soluções **Kafka-protocol compatible** permitem troca de broker apenas via configuração (bootstrap servers, credenciais) — zero alteração de código. Soluções com protocolo diferente exigem troca da biblioteca cliente e refatoração das anotações `@KafkaListener`, `@KafkaHandler`, producers, etc.

**Critérios eliminatórios**: FR-006 (zero alteração de código) + FR-009 (multi-cloud obrigatório).

---

## Candidatos Avaliados

### 1. AutoMQ ⭐ RECOMENDADO — Melhor TCO + Verdadeiramente Multi-Cloud

**Categoria**: Kafka-compatível · S3-native · Self-hosted / BYOC  
**Licença**: Apache 2.0 (open source verdadeiro — sem restrições)

**Gates**: G1 ✅ · G2 ✅ · G3 ✅ · G4 ✅ — **Passa todos os critérios eliminatórios**

**Multi-cloud**: Funciona com qualquer storage S3-compatible:
- AWS S3
- Google Cloud Storage (GCS) — com S3-compatible API
- Azure Blob Storage — com S3-compatible layer
- MinIO (self-hosted, qualquer cloud ou on-prem)
- **Resultado**: trocar de AWS para GCP = apenas mudar endpoint do bucket. Zero alteração de código ou configuração de aplicação.

**O que é**: Fork do Apache Kafka que substitui o armazenamento em disco local por S3-compatible object storage, mantendo 100% de compatibilidade com o protocolo Kafka. Os agentes são stateless — sem estado local, sem rebalanceamento de partições ao escalar.

**Compatibilidade Spring Kafka**: ✅ Drop-in. Apenas muda `spring.kafka.bootstrap-servers`. Zero alteração de código.

**Estimativa de custo (baseline: Kafka ~$500/mês em produção)**:
- Storage EBS gp3: $0.08/GiB vs S3: $0.023/GiB → redução de ~71% no storage
- Sem replicação cross-AZ nos brokers (S3 já faz isso)
- Pode usar Spot instances para compute (stateless = sem impacto de interrupções)
- **Redução total estimada: 50–90%** → ~$50–250/mês para carga CRM típica
- **Supera o critério de 30% (FR-008)** com folga

**Suporte a padrões CRM**:
| Padrão | Suporte |
|---|---|
| pub/sub | ✅ Nativo |
| Consumer groups | ✅ Nativo (protocolo Kafka) |
| Retry / DLT | ✅ Spring Kafka trata (não muda) |
| Self-consume (crm-email) | ✅ Funciona igual ao Kafka |
| Multi-consumer (${KAFKA_TOPIC}) | ✅ Consumer groups independentes |
| Replicação externa (IBGE) | ✅ Protocolo Kafka, sistema externo só muda o endpoint |

**Limitações**:
- Requer infraestrutura com bucket S3-compatible (qualquer cloud ou on-prem MinIO)
- Latência ligeiramente maior vs. Kafka com SSDs locais (write vai para S3 WAL) — aceitável para padrões CRM (não é HFT)
- Operação ainda requer conhecimento de Kafka (mas muito simplificada vs. Kafka full)

**Esforço de migração**: DROP-IN (apenas config)

---

### 2. WarpStream — ❌ ELIMINADO (G3: lock-in via aquisição Confluent)

**Categoria**: Kafka-compatível · S3-native · Managed (BYOC)  
**Licença**: Proprietária (Confluent — adquiriu WarpStream em 2024)

**Gate G3 — FALHOU**: A aquisição pela Confluent cria dependência de vendor proprietário. Confluent pode:
- Alterar a precificação unilateralmente sem aviso
- Restringir features do tier gratuito/barato
- Exigir migração para plataforma Confluent Cloud no futuro
- Descontinuar o produto WarpStream independente

Tecnicamente funciona em múltiplos clouds (BYOC com S3 em AWS/GCP/Azure), mas o **controle do plano de controle** está na Confluent — não é verdadeiramente multi-cloud nem portável.

**Compatibilidade Spring Kafka**: ✅ Drop-in. Protocolo Kafka.

**Estimativa de custo**:
- Cobra por GiB escrito/lido
- Claims de até 80% mais barato que Kafka tradicional
- **Risco**: Confluent pode mudar precificação a qualquer momento

**Suporte a padrões CRM**:
| Padrão | Suporte |
|---|---|
| pub/sub | ✅ |
| Consumer groups | ✅ |
| Retry / DLT | ✅ (Spring Kafka trata) |
| Self-consume | ✅ |
| Multi-consumer | ✅ |

**Limitações**:
- Aquisição pela Confluent é risco de lock-in e aumento de preço no médio prazo
- Latência p99 maior (S3 round-trips), mas aceitável para a maioria dos casos CRM

**Esforço de migração**: DROP-IN (apenas config)

---

### 3. Upstash Kafka ⭐ QUALIFICADO — Zero Ops + Custo Fixo Previsível

**Categoria**: Kafka-compatível · Serverless · SaaS Multi-região  
**Licença**: SaaS (Upstash — empresa independente)

**Gates**: G1 ✅ · G2 ✅ · G3 ✅ · G4 ✅ — **Passa todos os critérios eliminatórios**

**Multi-cloud**: Upstash é um SaaS independente com regiões em múltiplos cloud providers (AWS us-east-1, AWS eu-west-1, GCP us-central1, etc.). O CRM conecta ao endpoint Upstash — não há dependência direta do cloud provider subjacente. Trocar de provider: Upstash gerencia internamente.

**O que é**: Kafka serverless. Sem brokers para gerenciar. Protocolo Kafka via TLS/SASL. Pay-per-message com teto mensal.

**Compatibilidade Spring Kafka**: ✅ Drop-in. Protocolo Kafka via TLS.

**Preços atuais (2026)**:
- **Single-zone**: $0.20/100K mensagens — cap de **$120/mês**
- **Multi-zone**: $0.60/100K mensagens — cap de **$360/mês**

**Estimativa de custo CRM**:
- Cenário moderado: 1M mensagens/dia = 30M/mês
  - Single-zone: 30M / 100K × $0.20 = **$60/mês** (abaixo do cap)
  - Multi-zone: 30M / 100K × $0.60 = **$180/mês** (abaixo do cap de $360)
- **Redução vs. Kafka ~$500/mês: 64–88%** — supera FR-008

**Suporte a padrões CRM**:
| Padrão | Suporte |
|---|---|
| pub/sub | ✅ |
| Consumer groups | ✅ |
| Retry / DLT | ✅ (Spring Kafka trata) |
| Self-consume | ✅ |
| Multi-consumer | ✅ |
| Replicação externa | ⚠️ Sistema externo precisa suportar TLS/SASL para endpoint Upstash |

**Limitações**:
- Retenção máxima de 7 dias (Kafka pode ter retenção maior)
- Sem acesso a métricas avançadas de broker (vs. Kafka JMX)
- Para volume alto (>100M msg/mês), custo pode ser comparável ao Kafka gerenciado
- SLA e suporte dependem do plano pago; empresa de médio porte (risco de continuidade vs. AutoMQ Apache 2.0)

**Esforço de migração**: DROP-IN (apenas config + credenciais TLS)

---

### 4. RedPanda Self-hosted ⭐ QUALIFICADO — Menor Latência + Deploy em Qualquer Cloud

**Categoria**: Kafka-compatível · Self-hosted · Multi-cloud  
**Licença**: BSL 1.1 (source-available — uso interno sem restrições práticas)

**Gates**: G1 ✅ · G2 ✅ · G3 ✅ · G4 ✅ — **Passa todos os critérios (self-hosted)**

**Multi-cloud (Self-hosted)**: Deploy em qualquer VM/container em qualquer cloud (AWS, GCP, Azure, on-prem). A licença BSL 1.1 restringe apenas uso *competitivo* (oferecer RedPanda como serviço) — uso interno CRM não é afetado. Portabilidade total: mover cloud = redeployar os containers.

**Atenção — RedPanda Cloud (managed)**: A versão managed tem planos em AWS, GCP e Azure, mas é um vendor SaaS. Para garantir G3, preferir self-hosted.

**O que é**: Substituto do Kafka escrito em C++ (sem JVM, sem ZooKeeper/KRaft). Menor footprint de recursos. Menor latência de todos os candidatos.

**Compatibilidade Spring Kafka**: ✅ Drop-in. Protocolo Kafka.

**Estimativa de custo**:
- Self-hosted: ~3x t3.medium (menos brokers necessários) ≈ **$150–300/mês** + storage EBS
- Redução vs. Kafka: 40–60% — passa FR-008, mas menor que AutoMQ

**Limitações importantes**:
- BSL 1.1: não é Apache 2.0 (risco de mudança de licença no futuro — precedente: HashiCorp/Terraform)
- Community Edition não tem tiered storage (S3) — armazenamento em disco local ainda
- Redução de custo menor que AutoMQ porque ainda usa disco local (não S3-native)
- Preços do RedPanda Cloud aumentando desde 2025

**Melhor para**: Equipes que priorizam latência ultrabaixa e preferem self-hosted + Apache-like ops

**Esforço de migração**: DROP-IN (apenas config)

---

### 5. Amazon MSK Serverless — ❌ ELIMINADO (G3: AWS lock-in + custo alto)

**Categoria**: Kafka gerenciado · AWS-only  
**Licença**: SaaS (AWS)

**Gate G3 — FALHOU**: Exclusivo da AWS. Migrar para GCP ou Azure exigiria migração completa de plataforma (reconfiguração de auth IAM, endpoints proprietários, integrações AWS-específicas).

**Gate FR-008 — FALHOU adicionalmente**: $400–700/mês — pode ser *mais caro* que o Kafka atual.

**Decisão**: **ELIMINADO** por violar G3 (multi-cloud) e FR-008 (custo).

---

### 6. RabbitMQ — ❌ ELIMINADO (G1: requer mudança de código)

**Categoria**: Não Kafka-compatible · AMQP

**Gate G1 — FALHOU**: Requer troca de Spring Kafka por Spring AMQP em todos os 19 serviços. Viola FR-006.

**Decisão**: **ELIMINADO** por violar G1 e FR-006.

---

### 7. Amazon SQS + SNS — ❌ ELIMINADO (G1 + G2 + G3)

**Categoria**: Não Kafka-compatible · AWS-only · Serverless

**Gates falhados**: G1 (requer Spring Cloud AWS), G2 (sem consumer groups nativos), G3 (AWS lock-in).

**Decisão**: **ELIMINADO** por violar G1, G2 e G3.

---

## Comparativo Consolidado (Apenas Candidatos Qualificados)

| Critério | **AutoMQ** ⭐ | **Upstash Kafka** ⭐ | **RedPanda Self-hosted** |
|---|---|---|---|
| Gate G1 — Spring Kafka drop-in | ✅ | ✅ | ✅ |
| Gate G2 — Consumer groups | ✅ | ✅ | ✅ |
| Gate G3 — Multi-cloud | ✅ qualquer S3 | ✅ SaaS independente | ✅ self-hosted |
| Gate G4 — Portabilidade de dados | ✅ S3 portável | ✅ exportação disponível | ✅ dados em disco local |
| Custo estimado/mês | **$50–250** | **$60–180** | $150–300 |
| Redução de custo (FR-008 ≥30%) | ✅ **50–90%** | ✅ **64–88%** | ✅ 40–60% |
| Licença | **Apache 2.0** | SaaS | BSL 1.1 |
| Ops overhead | Médio (BYOC S3) | **Zero** | Médio (self-hosted) |
| Latência p99 | ~5–20ms | ~10–30ms | **~1–5ms** |
| Risco de lock-in | **Mínimo** | Baixo | Baixo-médio (BSL) |
| Portabilidade cloud | S3 de qualquer provider | Upstash gerencia | Qualquer VM/container |
| Esforço migração | Config only | Config only | Config only |

### Candidatos eliminados (resumo)

| Broker | Gate violado | Motivo |
|---|---|---|
| WarpStream | G3 | Adquirido pela Confluent — lock-in de vendor |
| MSK Serverless | G3 + FR-008 | AWS-only + mais caro que Kafka |
| RabbitMQ | G1 | Requer Spring AMQP — mudança de código |
| Amazon SQS/SNS | G1 + G2 + G3 | Sem drop-in, sem consumer groups, AWS-only |

---

## Decisão Recomendada

### Opção Principal: **AutoMQ** ⭐⭐

**Rationale** (ordenado por prioridade):
1. **Custo primário**: 50–90% de redução — maior de todos os candidatos qualificados
2. **Multi-cloud real**: funciona com S3 de AWS, GCP GCS, Azure Blob ou MinIO on-prem — trocar cloud = mudar 1 variável de ambiente
3. **Sem lock-in de licença**: Apache 2.0 — sem risco de mudança de licença (diferente de BSL do RedPanda)
4. **Sem lock-in de vendor**: dados ficam no seu bucket S3 — você controla completamente
5. Drop-in com Spring Kafka (FR-006)
6. Stateless agents = escala elástica com spot instances

**Trade-off**: Requer gestão de infraestrutura (bucket S3 + agents). Não é zero-ops.

### Opção Alternativa: **Upstash Kafka** ⭐

**Quando usar em vez de AutoMQ**:
- Equipe sem capacidade operacional de manter infraestrutura (zero ops é requisito)
- Volume CRM abaixo de ~3M msgs/dia (custo fica em $60–180/mês — cap previsível)
- PoC rápida em horas (não dias)

**Trade-off**: Retenção máxima 7 dias; dados gerenciados por terceiro; risco de continuidade do vendor.

### Sequência sugerida de PoC

1. **PoC 1**: AutoMQ em staging (2 dias) → domínios PRINTER e EMAIL
2. **PoC 2**: Upstash Kafka em staging (1 dia) — validar custo real vs. projeção
3. **Decisão final** baseada em custo real + operabilidade

---

## Referências

- [AutoMQ vs Kafka comparison (AutoMQ Blog)](https://www.automq.com/automq-vs-kafka)
- [Kafka Alternatives Compared 2026 (AutoMQ Blog)](https://www.automq.com/blog/kafka-alternatives-compared-2026)
- [RedPanda TCO vs Kafka](https://www.redpanda.com/data-streaming/platform-tco)
- [WarpStream S3-native Kafka](https://www.warpstream.com/blog/kafka-is-dead-long-live-kafka)
- [Upstash Kafka Pricing 2026](https://upstash.com/blog/serverless-kafka-launch)
- [RedPanda vs Kafka 2026 (AutoMQ Blog)](https://www.automq.com/blog/redpanda-vs-kafka-benchmark-cost-analysis)
- [Kafka Alternatives 2026 (Estuary)](https://estuary.dev/blog/kafka-alternatives/)
