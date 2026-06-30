# AutoMQ vs RedPanda — Comparativo de Migração Kafka

> Contexto: ambos foram avaliados como substitutos drop-in do Apache Kafka para o ecossistema CRM (19 serviços Spring Kafka, 30+ tópicos). Baseline de custo atual: ~$500/mês.

---

## Comparativo Geral

| Critério | AutoMQ | RedPanda |
|---|---|---|
| **Arquitetura** | Kafka fork — armazenamento em S3/object storage, agentes stateless | Rewrite em C++ — armazenamento local em disco, single-binary |
| **Drop-in Spring Kafka** | ✅ Apenas muda `bootstrap-servers` | ✅ Apenas muda `bootstrap-servers` |
| **Consumer Groups** | ✅ Nativo (protocolo Kafka) | ✅ Nativo (protocolo Kafka) |
| **Multi-cloud** | ✅ Qualquer S3-compatible (AWS, GCP, Azure, MinIO) | ✅ Self-hosted em qualquer VM/container |
| **Licença** | Apache 2.0 (open source verdadeiro) | BSL 1.1 (source-available, uso interno ok) |
| **JVM necessária** | ✅ Sim (fork do Kafka) | ❌ Não (C++ puro) |
| **ZooKeeper / KRaft** | ❌ Não precisa | ❌ Não precisa |
| **Testcontainers** | ⚠️ Usa imagem genérica Kafka | ✅ Suporte nativo (`org.testcontainers:redpanda`) |
| **Setup local (dev)** | 3 containers: MinIO + minio-setup + automq | 1 container: redpanda |
| **Latência p99** | ~5–20 ms | ~1–5 ms (melhor de todos) |

---

## Pontos Positivos e Negativos

| | AutoMQ | RedPanda |
|---|---|---|
| **Positivos** | ✅ Custo storage 71% menor (S3 vs EBS) | ✅ Latência mais baixa de todos os candidatos |
| | ✅ Escala elástica sem rebalanceamento de partições | ✅ Setup local simples (1 container) |
| | ✅ Stateless = usa spot instances (economia extra) | ✅ Testcontainers com suporte oficial |
| | ✅ Sem lock-in de licença (Apache 2.0) | ✅ Menor footprint de CPU/RAM (sem JVM) |
| | ✅ Multi-cloud real: trocar cloud = mudar 1 env var | ✅ Operação familiar (similar ao Kafka tradicional) |
| | ✅ Dados ficam no seu bucket (você controla) | ✅ Passa todos os gates CRM (G1–G4) |
| **Negativos** | ❌ Setup local mais complexo (3 containers + MinIO) | ❌ Storage em disco local (EBS) = mais caro que S3 |
| | ❌ Latência maior que RedPanda (S3 write path) | ❌ BSL 1.1: risco de mudança de licença (precedente HashiCorp) |
| | ❌ Ainda requer gestão de infraestrutura (não é zero-ops) | ❌ Community Edition sem tiered storage (S3 requer enterprise) |
| | ❌ Menor maturidade de mercado que RedPanda | ❌ Custo de storage não reduz tanto quanto AutoMQ |
| | ❌ Testcontainers via imagem genérica (menos ergonômico) | ❌ Escala horizontal mais trabalhosa que AutoMQ |

---

## Custo Estimado

| Item | AutoMQ | RedPanda (Self-hosted) |
|---|---|---|
| **Compute** | 2× t3.small (stateless, spot ok) ≈ **$15–30/mês** | 3× t3.medium (stateful, não spot) ≈ **$90–150/mês** |
| **Storage** | S3: $0.023/GiB/mês | EBS gp3: $0.08/GiB/mês |
| **100 GiB de dados** | ≈ $2,30/mês | ≈ $8,00/mês |
| **Replicação cross-AZ** | ❌ S3 já é multi-AZ por padrão (incluso) | ✅ Necessário replicar entre AZs manualmente |
| **Total estimado/mês** | **$50–250** | **$150–300** |
| **Redução vs. Kafka (~$500/mês)** | **50–90%** | **40–60%** |
| **Redução de storage** | ~71% mais barato por GiB | ~41% mais barato (sem tiered storage) |

---

## O Que Vale Mais a Pena?

### Depende do cenário — mas AutoMQ ganha na maioria dos casos CRM:

| Cenário | Recomendado | Motivo |
|---|---|---|
| **Volume alto (>50 GiB dados / mês)** | ✅ AutoMQ | Diferença de custo de storage se torna significativa em escala |
| **Prioridade em latência ultrabaixa** | ✅ RedPanda | Latência p99 de 1–5 ms vs 5–20 ms do AutoMQ |
| **Time sem expertise de infra cloud** | ✅ RedPanda | Setup mais simples, operação mais parecida com Kafka |
| **Multi-cloud obrigatório** | ✅ AutoMQ | S3-native garante portabilidade real; RedPanda depende de redeployar VMs |
| **Desenvolvimento / testes locais** | ✅ RedPanda | `testcontainers:redpanda` é mais ergonômico e o docker-compose é 1 container |
| **Preocupação com licença de longo prazo** | ✅ AutoMQ | Apache 2.0 sem risco de mudança; BSL do RedPanda tem precedente negativo (HashiCorp) |
| **Escala elástica com spot instances** | ✅ AutoMQ | Stateless = interrupções de spot não causam perda de dados |

### Recomendação Final

**AutoMQ para produção, RedPanda para dev/test** é a combinação mais equilibrada:

- **Produção**: AutoMQ oferece 50–90% de redução de custo vs. Kafka, portabilidade multi-cloud real e sem risco de licença. Para o volume CRM, a diferença de latência (5–20 ms vs 1–5 ms) é irrelevante — nenhum dos padrões do CRM é HFT.
- **Dev/Test**: RedPanda via Testcontainers é mais ergonômico e tem setup mais simples para rodar localmente, como demonstrado no módulo `system-async-kafka-migrate-redpanda`.

Se o time precisar escolher apenas **um** para produção: **AutoMQ** — menor TCO, maior portabilidade, licença mais segura.
