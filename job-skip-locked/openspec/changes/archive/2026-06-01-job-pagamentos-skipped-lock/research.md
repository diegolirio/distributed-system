# Research: ShedLock vs Skip Locked

Análise comparativa das duas abordagens para coordenação de jobs distribuídos em ambientes multi-instância Spring Boot.

---

## Contexto

Em ambientes com múltiplas instâncias de uma aplicação Spring Boot, jobs agendados (`@Scheduled`) precisam de coordenação para evitar processamento duplicado. Duas estratégias comuns são:

- **ShedLock** — mutex distribuído no nível do job (apenas uma instância executa por vez)
- **Skip Locked** — lock no nível da linha via SQL (`SELECT ... FOR UPDATE SKIP LOCKED`)

---

## ShedLock

**O que é**: Biblioteca (`net.javacrumbs.shedlock`) que transforma um `@Scheduled` em um mutex distribuído. Usa uma tabela `shedlock` no banco (ou Redis, ZooKeeper, etc.) para garantir que o job rode em **somente uma instância por vez**.

```
Instância A ──► @SchedulerLock ──► ADQUIRE lock ──► executa job
Instância B ──► @SchedulerLock ──► lock ocupado  ──► SKIP (não executa)
Instância C ──► @SchedulerLock ──► lock ocupado  ──► SKIP (não executa)
```

**Como usar**:
```java
@Scheduled(fixedDelay = 1000)
@SchedulerLock(name = "jobPagamentos", lockAtMostFor = "10m", lockAtLeastFor = "1s")
public void run() { ... }
```

### Pros

| # | Ponto |
|---|-------|
| 1 | Simples de adicionar — anotação + dependência |
| 2 | Não altera o schema da tabela de negócio |
| 3 | Funciona com qualquer query existente |
| 4 | Ideal para jobs que **não podem rodar em paralelo** (relatório, e-mail batch, cálculo de totais) |
| 5 | Suporta múltiplos backends (JDBC, Redis, ZooKeeper, Mongo) |

### Cons

| # | Ponto |
|---|-------|
| 1 | **Serializa o processamento** — instâncias adicionais ficam ociosas |
| 2 | Não escala horizontalmente — mais instâncias não = mais throughput |
| 3 | Se a instância travar/morrer, o lock fica preso até `lockAtMostFor` expirar |
| 4 | Depende de biblioteca externa + tabela de controle adicional (`shedlock`) |

---

## Skip Locked

**O que é**: Recurso nativo do banco de dados (Oracle 11g+, PostgreSQL 9.5+) que permite múltiplas instâncias buscarem linhas em paralelo, cada uma recebendo um subconjunto **exclusivo e não sobreposto**, sem bloqueio entre si.

```
Instância A ──► SELECT ... FOR UPDATE SKIP LOCKED (limit 100) ──► linhas  1–100 (locked)
Instância B ──► SELECT ... FOR UPDATE SKIP LOCKED (limit 100) ──► linhas 101–200 (locked)
Instância C ──► SELECT ... FOR UPDATE SKIP LOCKED (limit 100) ──► linhas 201–300 (locked)
```

**Fluxo do job**:

```
JOB
 │
 ▼
┌─────────────────────────────────┐
│  FindBy100 SKIP LOCKED          │◄──────────────────┐
└─────────────────────────────────┘                   │
         │                                            │
         ▼                                            │
    ┌─────────┐                                       │
    │ Há item?│                                       │
    └─────────┘                                       │
     Não │  │ Sim                                     │
         │  ▼                                         │
         │  Valida proposta + certificado             │
         │         │                                  │
         │         ▼                                  │
         │  Grava Pagamento + Lote                    │
         │         │                                  │
         │         ▼                                  │
         │  Finaliza (PROCESSED / FAILED)─────────────┘
         │
         ▼
       FIM
```

**Como usar** (Spring Data JPA):
```java
@Query(
  value = "SELECT * FROM requests WHERE status = 'PENDING' " +
          "FETCH FIRST :batchSize ROWS ONLY FOR UPDATE SKIP LOCKED",
  nativeQuery = true
)
List<Request> findBatchForUpdateSkipLocked(@Param("batchSize") int batchSize);
```

### Pros

| # | Ponto |
|---|-------|
| 1 | **Escala horizontalmente** — N instâncias processam N batches em paralelo |
| 2 | Crash recovery automático — lock liberado via rollback da transação |
| 3 | Sem ponto único de falha |
| 4 | Sem biblioteca externa — SQL puro |
| 5 | Ideal para filas de tarefas em tabela relacional |

### Cons

| # | Ponto |
|---|-------|
| 1 | H2 não suporta `SKIP LOCKED` — testes requerem TestContainers |
| 2 | Exige coluna de status (`PENDING`/`PROCESSING`/`PROCESSED`/`FAILED`) na tabela de negócio |
| 3 | Query nativa (`nativeQuery = true`) — menos portável entre dialetos |
| 4 | Mais complexo de implementar corretamente (gerenciar status, transações `REQUIRES_NEW`) |

---

## Comparação lado a lado

| Critério | ShedLock | Skip Locked |
|---------|----------|-------------|
| Escala horizontal | ✗ serializa | ✓ paralelo |
| Crash recovery | manual (`lockAtMostFor`) | ✓ automático (rollback) |
| Mudança no schema | ✗ nenhuma | coluna `status` necessária |
| Dependência externa | biblioteca + tabela shedlock | nenhuma |
| Suporte a H2 (testes) | ✓ | ✗ (precisa TestContainers) |
| Portabilidade SQL | ✓ | Oracle 11g+ / PostgreSQL 9.5+ |
| Complexidade de impl. | baixa | média |
| Throughput multi-instância | igual a 1 instância | linear com instâncias |

---

## Quando usar cada um

| Cenário | Recomendação |
|---------|-------------|
| Job de relatório / envio de e-mail — não pode rodar duas vezes ao mesmo tempo | **ShedLock** |
| Fila de tarefas na tabela — precisa de throughput, múltiplas instâncias | **Skip Locked** |
| Ambiente com apenas uma instância de app | Qualquer um (ShedLock é mais simples) |
| Recuperação automática se instância morrer no meio do processamento | **Skip Locked** |
| Time sem familiaridade com SQL avançado / locks | **ShedLock** |
| Job leve que não vale a complexidade de controle de status | **ShedLock** |

---

## Podem ser usados juntos?

Sim. Um padrão robusto combina os dois:

- **ShedLock** garante que o loop de polling não seja iniciado por N instâncias ao mesmo tempo (evita explosão de conexões).
- **Skip Locked** garante que dentro do loop cada linha seja processada por exatamente uma instância.

```
ShedLock ──► garante 1 instância inicia o job por vez
Skip Locked ──► garante que cada linha é processada exatamente uma vez
```

Útil quando o batch é grande e você quer paralelismo no processamento mas controle de entrada.

---

## Decisão para o JobPagamentos

O fluxo do `JobPagamentos` é uma **fila de processamento** — exatamente o caso de uso ideal de Skip Locked:

- Múltiplas instâncias processam batches em paralelo → throughput linear
- Crash no meio do processamento reverte automaticamente a transação → linha volta a `PENDING`
- Sem coordenação externa necessária

ShedLock seria redundante aqui, a não ser que você queira limitar o número de instâncias que entram no loop simultaneamente (preocupação de pool de conexões).
