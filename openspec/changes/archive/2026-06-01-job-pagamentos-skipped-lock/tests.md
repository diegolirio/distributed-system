# Testes — Job Pagamentos com SKIP LOCKED

> Documento de especificação de testes de integração e E2E.
> Tecnologia-agnóstico: use HTTP padrão para o endpoint e SQL padrão para validações de banco.

---

## Contexto

A feature introduz um job agendado (`JobPagamentos`) que processa registros da tabela `requests` em um loop contínuo usando `SELECT ... FOR UPDATE SKIP LOCKED`, garantindo que cada linha seja processada exatamente uma vez mesmo em ambientes com múltiplas instâncias. Cada linha é validada, um registro `Pagamento` é criado e associado a um `Lote` do dia, e o status da linha é atualizado atomicamente. A mesma lógica de processamento é exposta via endpoint REST `POST /api/pagamentos/processar`, permitindo disparo sob demanda.

**Tabelas envolvidas:** `requests`, `pagamentos`, `lotes`
**Endpoint:** `POST /api/pagamentos/processar`

---

## Estrutura da Requisição (Endpoint)

```
HTTP Method : POST
URL         : /api/pagamentos/processar
Content-Type: application/json

Body: (sem body — disparo sem parâmetros)
```

## Estrutura da Resposta (sucesso)

```
HTTP Status : 200
Content-Type: application/json

Body:
{
  "processed": <Long>,   -- quantidade de linhas processadas com sucesso
  "failed":    <Long>    -- quantidade de linhas marcadas como FAILED
}
```

---

## Casos de Teste

---

### Grupo 1 — Job Pagamentos (Loop de Execução)

---

### CT-01 — Loop executa até esgotar linhas PENDING

**Descrição:** O job deve continuar buscando e processando batches até que nenhuma linha com status `PENDING` reste na tabela `requests`.

**Pré-condição:** 250 linhas válidas com status `PENDING` existem na tabela `requests`.

**Entrada:**
```
Evento: disparo do job agendado
Configuração: app.job.batch-size=100, app.job.enabled=true
```

**Critérios de Aceite:**
- O job executa 3 iterações (100 + 100 + 50 linhas)
- Após a conclusão, `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `250`
- `SELECT COUNT(*) FROM pagamentos` retorna `250`
- O job não lança exceção nem encerra prematuramente

---

### CT-02 — Loop termina quando nenhuma linha PENDING é encontrada

**Descrição:** O job deve encerrar o loop normalmente ao receber um batch vazio, sem erro e sem consumo excessivo de recursos.

**Pré-condição:** Tabela `requests` não possui linhas com status `PENDING`.

**Entrada:**
```
Evento: disparo do job agendado
Configuração: app.job.enabled=true
```

**Critérios de Aceite:**
- O job executa exatamente uma consulta à base e retorna sem processar nenhuma linha
- `SELECT COUNT(*) FROM pagamentos` permanece inalterado
- Nenhuma exceção é lançada

---

### CT-03 — Tamanho do batch é respeitado conforme configuração

**Descrição:** Quando `app.job.batch-size` é configurado com um valor menor que o padrão, cada iteração do loop deve processar no máximo esse número de linhas.

**Pré-condição:** 200 linhas válidas com status `PENDING` existem na tabela `requests`.

**Entrada:**
```
Evento: disparo do job agendado
Configuração: app.job.batch-size=50, app.job.enabled=true
```

**Critérios de Aceite:**
- O job executa 4 iterações de exatamente 50 linhas cada
- Após a conclusão, `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM pagamentos` retorna `200`

---

### CT-04 — Job desabilitado não consulta o banco

**Descrição:** Quando `app.job.enabled=false`, o job deve retornar imediatamente sem executar nenhuma query.

**Pré-condição:** 50 linhas com status `PENDING` existem na tabela `requests`.

**Entrada:**
```
Evento: disparo do job agendado
Configuração: app.job.enabled=false
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` permanece em `50` após o disparo
- `SELECT COUNT(*) FROM pagamentos` permanece inalterado
- Nenhuma query `SELECT ... FOR UPDATE SKIP LOCKED` é executada

---

### Grupo 2 — Repositório SKIP LOCKED

---

### CT-05 — Linhas disponíveis são retornadas e bloqueadas exclusivamente

**Descrição:** Quando há linhas `PENDING` e nenhuma outra transação as bloqueia, o repositório deve retornar até N linhas e bloqueá-las para a transação corrente.

**Pré-condição:** 200 linhas com status `PENDING` existem; nenhuma outra transação ativa.

**Entrada:**
```
Chamada: findTop100ForUpdateSkipLocked()
N: 100
```

**Critérios de Aceite:**
- O método retorna exatamente 100 linhas
- Uma segunda transação concorrente que execute a mesma query retorna as 100 linhas restantes (sem sobreposição)
- Nenhuma linha é retornada em ambas as chamadas simultâneas

---

### CT-06 — Todas as linhas PENDING bloqueadas por outra instância — retorna lista vazia

**Descrição:** Quando todas as linhas `PENDING` estão bloqueadas por transações concorrentes, o repositório deve retornar uma lista vazia sem bloquear nem lançar exceção.

**Pré-condição:** Todas as linhas `PENDING` estão dentro de uma transação ativa em outro processo.

**Entrada:**
```
Chamada: findTop100ForUpdateSkipLocked()
```

**Critérios de Aceite:**
- O método retorna uma lista vazia (`size = 0`)
- Nenhuma exceção é lançada
- O tempo de resposta não depende do tempo de lock da outra transação (sem bloqueio)

---

### CT-07 — Mix de linhas bloqueadas e desbloqueadas

**Descrição:** Quando parte das linhas `PENDING` está bloqueada e outra parte está livre, o repositório deve retornar apenas as linhas livres (até N).

**Pré-condição:** 150 linhas com status `PENDING`; 80 bloqueadas por outra transação; N=100.

**Entrada:**
```
Chamada: findTop100ForUpdateSkipLocked()
N: 100
```

**Critérios de Aceite:**
- O método retorna exatamente 70 linhas (as não bloqueadas)
- Nenhuma das 80 linhas bloqueadas está na lista retornada
- `size(resultado) = 70`

---

### CT-08 — Status atualizado para PROCESSING antes do processamento

**Descrição:** Imediatamente após a linha ser selecionada pelo SKIP LOCKED, o status deve ser alterado para `PROCESSING` dentro da mesma transação, impedindo re-seleção.

**Pré-condição:** 1 linha com status `PENDING` existe.

**Entrada:**
```
Chamada: findTop100ForUpdateSkipLocked() seguida de atualização de status
```

**Critérios de Aceite:**
- Durante a execução do processamento, `SELECT status FROM requests WHERE id = ?` retorna `PROCESSING`
- Uma instância concorrente que execute a query SKIP LOCKED não retorna essa linha

---

### CT-09 — Status atualizado para PROCESSED após sucesso

**Descrição:** Quando o processamento de uma linha conclui sem erro, o status final deve ser `PROCESSED`.

**Pré-condição:** 1 linha com status `PENDING` e dados válidos (propostaId não nulo, valor > 0, certificado válido).

**Entrada:**
```
Linha: { propostaId: "P-001", valor: 150.00, certificado: <válido> }
```

**Critérios de Aceite:**
- `SELECT status FROM requests WHERE id = ?` retorna `PROCESSED`
- `SELECT COUNT(*) FROM pagamentos WHERE proposta_id = 'P-001'` retorna `1`
- `SELECT error_message FROM requests WHERE id = ?` retorna `null`

---

### CT-10 — Status atualizado para FAILED em erro de validação

**Descrição:** Quando o processamento lança `ValidationException`, o status deve ser `FAILED` e a mensagem de erro deve ser armazenada em `requests.error_message`.

**Pré-condição:** 1 linha com `propostaId` nulo (inválido).

**Entrada:**
```
Linha: { propostaId: null, valor: 100.00, certificado: <válido> }
```

**Critérios de Aceite:**
- `SELECT status FROM requests WHERE id = ?` retorna `FAILED`
- `SELECT error_message FROM requests WHERE id = ?` retorna `"propostaId is required"`
- `SELECT COUNT(*) FROM pagamentos WHERE proposta_id IS NULL` retorna `0` (nenhum pagamento criado)

---

### Grupo 3 — Processador de Pagamento

---

### CT-11 — Proposta e certificado válidos — processamento continua

**Descrição:** Quando todos os dados da proposta e o certificado são válidos, a validação deve passar e o processamento prosseguir até a persistência.

**Pré-condição:** Linha com `propostaId` não nulo, `valor` > 0 e certificado válido (não expirado).

**Entrada:**
```
Linha: { propostaId: "P-100", valor: 500.00, certificado: <não expirado> }
```

**Critérios de Aceite:**
- Nenhuma `ValidationException` é lançada
- `SELECT COUNT(*) FROM pagamentos WHERE proposta_id = 'P-100'` retorna `1`
- `SELECT status FROM requests WHERE proposta_id = 'P-100'` retorna `PROCESSED`

---

### CT-12 — propostaId ausente — ValidationException lançada

**Descrição:** Quando `propostaId` é nulo ou vazio, o processador deve rejeitar a linha sem criar nenhum registro financeiro.

**Pré-condição:** Linha com `propostaId = null`.

**Entrada:**
```
Linha: { propostaId: null, valor: 200.00, certificado: <válido> }
```

**Critérios de Aceite:**
- `ValidationException` é lançada com mensagem `"propostaId is required"`
- `SELECT COUNT(*) FROM pagamentos` não aumenta
- `SELECT status FROM requests WHERE id = ?` retorna `FAILED`
- `SELECT error_message FROM requests WHERE id = ?` retorna `"propostaId is required"`

---

### CT-13 — Certificado inválido ou expirado — ValidationException lançada

**Descrição:** Quando o certificado da proposta está expirado ou malformado, o processador deve rejeitar a linha sem persistir nada.

**Pré-condição:** Linha com `propostaId` válido e certificado expirado.

**Entrada:**
```
Linha: { propostaId: "P-200", valor: 300.00, certificado: <expirado> }
```

**Critérios de Aceite:**
- `ValidationException` é lançada com mensagem `"certificate invalid"`
- `SELECT COUNT(*) FROM pagamentos WHERE proposta_id = 'P-200'` retorna `0`
- `SELECT status FROM requests WHERE id = ?` retorna `FAILED`
- `SELECT error_message FROM requests WHERE id = ?` retorna `"certificate invalid"`

---

### CT-14 — Novo Lote criado para a data atual quando não existe

**Descrição:** Quando não existe nenhum `Lote` para a data de processamento atual, um novo deve ser criado e o `Pagamento` deve referenciá-lo.

**Pré-condição:** Tabela `lotes` não possui registro para `data_processamento = hoje`.

**Entrada:**
```
Linha: { propostaId: "P-300", valor: 100.00, certificado: <válido> }
Data: hoje
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM lotes WHERE data_processamento = hoje` retorna `1`
- `SELECT lote_id FROM pagamentos WHERE proposta_id = 'P-300'` aponta para o novo lote criado

---

### CT-15 — Lote existente reutilizado para a data atual

**Descrição:** Quando já existe um `Lote` para a data atual, nenhum novo deve ser criado; o `Pagamento` deve referenciar o `Lote` existente.

**Pré-condição:** 1 registro em `lotes` com `data_processamento = hoje` já existe.

**Entrada:**
```
Linha: { propostaId: "P-400", valor: 200.00, certificado: <válido> }
Data: hoje
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM lotes WHERE data_processamento = hoje` permanece em `1`
- `SELECT lote_id FROM pagamentos WHERE proposta_id = 'P-400'` aponta para o lote pré-existente

---

### CT-16 — Valor do pagamento registrado corretamente

**Descrição:** O campo `valor` do `Pagamento` criado deve ser igual ao `valor` da `Request`, sem truncamento nem arredondamento inesperado.

**Pré-condição:** Linha com `propostaId = "P-500"` e `valor = 1234.56`.

**Entrada:**
```
Linha: { propostaId: "P-500", valor: 1234.56 }
```

**Critérios de Aceite:**
- `SELECT valor FROM pagamentos WHERE proposta_id = 'P-500'` retorna `1234.56`
- `SELECT proposta_id FROM pagamentos WHERE proposta_id = 'P-500'` retorna `"P-500"`

---

### CT-17 — Falha em uma linha não cancela o processamento das demais

**Descrição:** Uma `ValidationException` em uma linha de um batch não deve reverter as linhas já processadas com sucesso nem impedir o processamento das linhas seguintes.

**Pré-condição:** Batch de 12 linhas: 10 válidas e 2 com `propostaId = null`.

**Entrada:**
```
Linhas: 10 com propostaId válido + 2 com propostaId = null
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `10`
- `SELECT COUNT(*) FROM requests WHERE status = 'FAILED'` retorna `2`
- `SELECT COUNT(*) FROM pagamentos` retorna `10`
- As 10 linhas válidas têm `error_message = null`
- As 2 linhas inválidas têm `error_message = "propostaId is required"`

---

### Grupo 4 — Endpoint REST

---

### CT-18 — Endpoint processa todas as linhas PENDING e retorna contagem

**Descrição:** Uma chamada ao endpoint deve processar todas as linhas `PENDING` disponíveis e retornar o total processado no body da resposta.

**Pré-condição:** 50 linhas válidas com status `PENDING` existem.

**Entrada:**
```
HTTP Method : POST
URL         : /api/pagamentos/processar
```

**Critérios de Aceite:**
- HTTP Status: `200`
- Body: `{ "processed": 50, "failed": 0 }`
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `50`
- `SELECT COUNT(*) FROM pagamentos` retorna `50`

---

### CT-19 — Endpoint retorna zero quando não há linhas PENDING

**Descrição:** Quando não há nenhuma linha `PENDING`, o endpoint deve retornar `HTTP 200` com contadores zerados, sem erro.

**Pré-condição:** Nenhuma linha com status `PENDING` na tabela `requests`.

**Entrada:**
```
HTTP Method : POST
URL         : /api/pagamentos/processar
```

**Critérios de Aceite:**
- HTTP Status: `200`
- Body: `{ "processed": 0, "failed": 0 }`
- Nenhum registro de `Pagamento` é criado

---

### CT-20 — Endpoint reporta linhas com falha separadamente

**Descrição:** Quando o batch contém linhas inválidas, o endpoint deve contabilizá-las no campo `failed` sem incluí-las em `processed`.

**Pré-condição:** 10 linhas válidas e 2 linhas com `propostaId = null`, todas com status `PENDING`.

**Entrada:**
```
HTTP Method : POST
URL         : /api/pagamentos/processar
```

**Critérios de Aceite:**
- HTTP Status: `200`
- Body: `{ "processed": 10, "failed": 2 }`
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `10`
- `SELECT COUNT(*) FROM requests WHERE status = 'FAILED'` retorna `2`
- `SELECT COUNT(*) FROM pagamentos` retorna `10`

---

### CT-21 — Endpoint e job concorrentes não duplicam processamento

**Descrição:** Quando o job agendado e o endpoint são acionados simultaneamente, cada linha da tabela `requests` deve ser processada exatamente uma vez.

**Pré-condição:** 100 linhas com status `PENDING`; job agendado e endpoint ambos ativos.

**Entrada:**
```
Evento: disparo simultâneo do job + POST /api/pagamentos/processar
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM pagamentos` retorna exatamente `100` (sem duplicatas)
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `100`
- Nenhum `propostaId` aparece mais de uma vez em `pagamentos`

---

### Grupo 5 — Testes de Integração (TestContainers)

---

### CT-22 — Container de banco de dados sobe e migrations são aplicadas

**Descrição:** Ao inicializar a suíte de integração, um container de banco de dados real deve subir e as migrations devem ser aplicadas antes de qualquer teste executar.

**Pré-condição:** Docker disponível no ambiente de execução.

**Entrada:**
```
Evento: inicialização da classe de teste de integração
```

**Critérios de Aceite:**
- O container responde a conexões JDBC antes do primeiro teste
- As tabelas `requests`, `pagamentos` e `lotes` existem após as migrations
- O schema não apresenta erros de constraint ou coluna ausente

---

### CT-23 — Testes de integração passam em CI sem banco local instalado

**Descrição:** Os testes devem funcionar em qualquer ambiente CI que possua Docker, sem dependência de banco de dados local pré-configurado.

**Pré-condição:** Ambiente CI com Docker disponível; sem instância de banco pré-existente.

**Entrada:**
```
Evento: execução via pipeline de CI
```

**Critérios de Aceite:**
- Todos os testes de integração completam sem `ConnectionRefusedException` nem erros de configuração de datasource
- O resultado final da suíte é `PASSED`

---

### CT-24 — Duas threads processam conjuntos disjuntos de linhas (SKIP LOCKED)

**Descrição:** Quando duas execuções do job rodam simultaneamente, nenhuma linha deve ser processada por ambas — o SKIP LOCKED garante particionamento exclusivo.

**Pré-condição:** 200 linhas com status `PENDING` inseridas; duas threads de job iniciadas simultaneamente.

**Entrada:**
```
Threads: 2 instâncias do job, cada uma com batch-size=100
```

**Critérios de Aceite:**
- A união dos IDs processados pela thread A e pela thread B contém exatamente 200 IDs únicos
- A interseção dos IDs processados pela thread A e B é vazia (sem sobreposição)
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `200`
- `SELECT COUNT(*) FROM pagamentos` retorna `200`

---

### CT-25 — Todas as linhas PENDING processadas ao final do job (E2E)

**Descrição:** Teste E2E completo: seeding, disparo do job, verificação de status final e existência de registros financeiros.

**Pré-condição:** 50 linhas válidas com status `PENDING` inseridas na tabela `requests`.

**Entrada:**
```
Evento: disparo do job
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `50`
- `SELECT COUNT(*) FROM pagamentos` retorna `50`
- `SELECT COUNT(*) FROM lotes` retorna `1` (todos associados ao mesmo lote do dia)

---

### CT-26 — Linhas inválidas marcadas como FAILED, válidas processadas normalmente

**Descrição:** Em um batch com linhas válidas e inválidas, as inválidas devem ser marcadas como `FAILED` sem interromper o processamento das válidas.

**Pré-condição:** 10 linhas com `propostaId` válido e 2 linhas com `propostaId = null`, todas com status `PENDING`.

**Entrada:**
```
Evento: disparo do job
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `10`
- `SELECT COUNT(*) FROM requests WHERE status = 'FAILED'` retorna `2`
- `SELECT COUNT(*) FROM pagamentos` retorna `10`
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`

---

### Grupo 6 — Teste Concorrente do Endpoint (500 linhas × 5 threads)

---

### CT-27 — 500 linhas PENDING inseridas antes das chamadas HTTP

**Descrição:** O setup do teste deve garantir que exatamente 500 linhas válidas estejam com status `PENDING` antes de qualquer chamada ao endpoint.

**Pré-condição:** Banco de dados limpo (sem linhas em `requests`).

**Entrada:**
```
Ação: inserção de 500 linhas válidas via setup do teste
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `500` antes de qualquer chamada HTTP

---

### CT-28 — 5 requisições HTTP disparadas simultaneamente

**Descrição:** As 5 chamadas ao endpoint devem ser submetidas de forma que todas iniciem antes de qualquer uma retornar, validando concorrência real.

**Pré-condição:** 500 linhas `PENDING` disponíveis; 5 threads aguardando sinal de liberação simultânea.

**Entrada:**
```
HTTP Method : POST
URL         : /api/pagamentos/processar
Threads     : 5, liberadas ao mesmo instante via CountDownLatch
```

**Critérios de Aceite:**
- Todas as 5 threads submetem a requisição antes de qualquer resposta ser recebida
- Nenhuma thread lança exceção de conexão ou timeout durante o disparo

---

### CT-29 — Todas as 5 requisições concorrentes retornam HTTP 200

**Descrição:** Cada uma das 5 chamadas simultâneas deve encerrar com `HTTP 200`, sem erros de servidor nem falhas de lock.

**Pré-condição:** 500 linhas `PENDING`; 5 threads disparadas simultaneamente.

**Entrada:**
```
HTTP Method : POST
URL         : /api/pagamentos/processar
Threads     : 5
```

**Critérios de Aceite:**
- Todas as 5 respostas têm HTTP Status `200`
- Nenhuma resposta contém status `500`, `503` ou `409`
- A soma de `processed` + `failed` de todas as respostas é igual a `500`

---

### CT-30 — Nenhuma linha PENDING restante após processamento concorrente

**Descrição:** Após todas as 5 chamadas concorrentes retornarem, nenhuma linha deve permanecer com status `PENDING` — prova de que o SKIP LOCKED funciona de ponta a ponta.

**Pré-condição:** 500 linhas `PENDING`; 5 threads concluídas.

**Entrada:**
```
Verificação pós-processamento
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` retorna `0`
- `SELECT COUNT(*) FROM requests WHERE status IN ('PROCESSED', 'FAILED')` retorna `500`

---

### CT-31 — Exatamente 500 registros Pagamento criados (sem duplicatas)

**Descrição:** O total de registros na tabela `pagamentos` após o processamento concorrente deve ser exatamente 500, comprovando que nenhuma linha foi processada mais de uma vez.

**Pré-condição:** 500 linhas `PENDING` (todas válidas); 5 threads concluídas.

**Entrada:**
```
Verificação pós-processamento
```

**Critérios de Aceite:**
- `SELECT COUNT(*) FROM pagamentos` retorna `500`
- `SELECT COUNT(DISTINCT proposta_id) FROM pagamentos` retorna `500` (sem `propostaId` duplicado)
- `SELECT COUNT(*) FROM requests WHERE status = 'PROCESSED'` retorna `500`

---

## Validações de Banco de Dados

### Cenário de sucesso (referência para CT-18, CT-25)

| Tabela | Verificação | Valor Esperado |
|--------|-------------|----------------|
| `requests` | `COUNT(*) WHERE status = 'PROCESSED'` | igual ao total de linhas válidas inseridas |
| `requests` | `COUNT(*) WHERE status = 'PENDING'` | `0` |
| `requests` | `COUNT(*) WHERE status = 'FAILED'` | igual ao total de linhas inválidas |
| `pagamentos` | `COUNT(*)` | igual ao total de linhas com status `PROCESSED` |
| `pagamentos` | `COUNT(DISTINCT proposta_id)` | igual a `COUNT(*)` (sem duplicatas) |
| `lotes` | `COUNT(*) WHERE data_processamento = hoje` | `1` (lote único por dia) |

### Cenário de rejeição (referência para CT-12, CT-13, CT-17)

| Tabela | Verificação | Valor Esperado |
|--------|-------------|----------------|
| `requests` | `COUNT(*) WHERE status = 'FAILED'` | igual ao número de linhas inválidas |
| `pagamentos` | `COUNT(*)` | igual apenas ao número de linhas válidas |
| `requests` | `error_message` das linhas FAILED | mensagem descritiva da `ValidationException` |

---

## Fora de Escopo

- Fila de dead-letter ou reprocessamento automático de linhas `FAILED` (marcar como `FAILED` e continuar é o comportamento esperado)
- Processamento assíncrono ou reativo (o processamento é síncrono por design)
- Rastreamento distribuído (distributed tracing) e dashboards de métricas
- Fan-out para mais de duas tabelas além de `Pagamento` e `Lote`
- Rollback de schema em caso de reversão (apenas desabilitar o job via configuração)
- Validação de certificado via serviço externo (escopo da validação é local)
