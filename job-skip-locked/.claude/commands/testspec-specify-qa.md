---
name: testspec-specify-qa
description: Start QA test specification for a feature, fetching tests.md via GitHub MCP, conducting a QA questionnaire and generating spec.qa.md and tasks.qa.md.
license: MIT
compatibility: Requires @analizza-ai/testspec and GitHub MCP access.
metadata:
  author: analizza-ai
  version: "0.1.2"
  generatedBy: testspec
---

# testspec-specify-qa

Inicia a especificação técnica de testes QA para uma feature, buscando o `tests.md` via GitHub MCP, conduzindo um questionário com o QA engineer e gerando os artefatos de especificação e estrutura de pastas no diretório atual do projeto QA.

## Quando usar

Após `/testspec-generate` ter gerado o `tests.md` da feature no repositório do app.

---

## Configuração

- **App repo:** acessado exclusivamente via GitHub MCP — owner/repo definido em `./testspec/instructions.md` via campo `app_repo` (ex: `diegolirio/sdd-sdt-flow`)
- **Nunca use caminhos relativos ou locais** para acessar o repositório do app — sempre GitHub MCP
- **Diretório base de saída:** **sempre o diretório de trabalho atual** (`.`) — nunca use caminhos absolutos ou hardcoded

---

## Instructions

### 0. Garantir e ler arquivos globais do projeto QA (SEMPRE, primeiro passo)

**Nunca peça ao usuário para criar arquivos ou diretórios manualmente. Crie tudo que faltar e prossiga.**

#### 0a. Garantir estrutura `./testspec/`

Se `./testspec/` não existir, crie o diretório.

#### 0b. Garantir `./testspec/instructions.md`

Se o arquivo não existir, pergunte ao usuário via **AskUserQuestion** (open-ended):
> "Qual é o repositório GitHub do app? (formato: owner/repo, ex: diegolirio/sdd-sdt-flow)"

Com a resposta, crie o arquivo com conteúdo padrão opinativo:

```markdown
# Instructions — QA Project

## app_repo
{owner/repo informado pelo usuário}

## Ferramenta de teste padrão
k6

## Arquitetura
- Scripts organizados por tipo: e2e/, load/, chaos-engineering/
- Um script por caso de teste (CT)
- Run plans .md gerados ao lado de cada script

## Tecnologias
- k6 (JavaScript) para E2E e load
- kubectl para coleta de logs k8s
- Confluence para publicação de relatórios

## Convenções
- Nomenclatura kebab-case sem acentos
- Variáveis de ambiente para URLs e credenciais (nunca hardcode)
- Thresholds: p95 < 500ms, error rate < 1% (load); p95 < 2000ms, error rate < 5% (chaos)
```

Após criar, leia o arquivo e aplique todas as diretrizes durante a geração — elas têm precedência sobre os defaults da skill.

#### 0c. Garantir `./testspec/current-feature.md`

Se o arquivo não existir, crie-o vazio e execute o passo 1 normalmente.
Se existir e contiver um nome de feature válido, use-o diretamente — pule o passo 1.

---

### 1. Identificar a feature

O argumento passado pelo usuário pode ser:
- **Nome da feature** em kebab-case (ex: `kafka-consumer-order-request`)
- **Nenhum argumento** — siga o passo 1b abaixo

> **Atenção:** se `./testspec/current-feature.md` já definiu a feature no passo 0, este passo é ignorado.

**Se nenhum argumento foi passado e `current-feature.md` está vazio:**

Leia `app_repo` de `./testspec/instructions.md` (ex: `diegolirio/sdd-sdt-flow`).

**Prioridade 1 — GitHub MCP**

Busque em paralelo via GitHub MCP usando o `app_repo` do `instructions.md`:
- Features **ativas**: `GET /repos/{app_repo}/contents/openspec/changes` — filtre `type: "dir"`, exclua `archive`
- Features **arquivadas**: `GET /repos/{app_repo}/contents/openspec/changes/archive` — filtre `type: "dir"`

**Prioridade 2 — Fallback: features já em `./testspec/`**

Se o GitHub MCP falhar (sem token, sem acesso), liste os diretórios em `./testspec/` excluindo `instructions.md` e `current-feature.md`. Prefixe com `[local]`.

Se nenhuma fonte retornar features, informe e encerre:
```
Nenhuma feature encontrada em {app_repo} via GitHub MCP nem em ./testspec/.
Verifique o token GitHub ou adicione features ao repositório.
```

a. Monte a lista completa e pergunte ao usuário via **AskUserQuestion** (single select):
   > "Qual feature deseja especificar os testes QA?"

   - Prefixe: `[ativa]`, `[arquivada]` ou `[local]`
   - Exiba o nome exato do diretório

b. Registre: nome da feature e sua origem.

### 1.1 Fixar a feature em `current-feature.md`

Independentemente de como a feature foi identificada (argumento, seleção ou `current-feature.md` já preenchido), **sempre escreva** o nome da feature no arquivo:

```
./testspec/current-feature.md
```

Conteúdo do arquivo (apenas o nome, sem formatação extra):
```
{feature-name}
```

Isso garante que `/testspec-apply-qa` e outras skills da sessão usem a mesma feature sem precisar perguntar novamente.

---

### 2. Ler o `tests.md`

Leia via GitHub MCP usando `app_repo` de `instructions.md`:
- Feature **ativa**: path `openspec/changes/{feature-name}/tests.md`
- Feature **arquivada**: path `openspec/changes/archive/{feature-name}/tests.md`

O conteúdo virá em base64 — decodifique antes de usar.

**Fallback — feature `[local]`** (veio de `./testspec/`):

Se a feature foi encontrada apenas em `./testspec/` e o GitHub MCP está inacessível, verifique se `spec.qa.md` já existe em `./testspec/{feature-name}/`:
- Se existe: pergunte via **AskUserQuestion**:
  > "Encontrei `spec.qa.md` existente para esta feature. Como deseja prosseguir?"
  - **Refazer** — ignora o existente e segue o questionário normalmente
  - **Usar o existente** — pula para o passo 8
- Se não existe: encerre informando:
  ```
  tests.md não encontrado via GitHub MCP.
  Verifique o token GitHub ou execute /testspec-generate no repositório do app.
  ```

---

### 3. Analisar o `tests.md` em profundidade

Extraia e internalize **todos** os seguintes dados antes de avançar:

- **Nome legível da feature** — título `# Testes —`
- **Protocolo de entrada** — HTTP, Kafka, gRPC (derivado de `## Contexto` e `## Estrutura`)
- **Método HTTP e path** — se protocolo HTTP
- **Schema do body de entrada** — campos, tipos, obrigatoriedade
- **Schema da resposta de sucesso** — status HTTP, headers (especialmente `Location`), body
- **Tabela(s) de banco de dados** — mencionadas em `## Validações de Banco de Dados`
- **Todos os `### CT-NN`** — número, nome, entrada, critérios de aceite, tipo (sucesso/rejeição)
- **Regras de negócio implícitas** — ex: "COUNT não muda", "campo preenchido com valor X"

---

### 4. Conduzir o questionário

Faça as perguntas **em sequência**, uma de cada vez — não agrupe em uma só mensagem.

---

#### Pergunta A — Ferramenta de testes

**AskUserQuestion** (single select):
> "Qual ferramenta será usada para implementar os scripts de teste?"

| Opção | Descrição |
|---|---|
| **K6** | JavaScript, ideal para E2E e load; extensão `.js` |
| **Gatling** | Scala/Java, foco em load e relatórios detalhados; extensão `.scala` |
| **Outro** | Abre campo livre para descrever ferramenta e extensão de arquivo |

Se "Outro": pergunte em seguida (open-ended) o nome da ferramenta e a extensão dos arquivos.

Registre: `{tool}` (ex: `k6`, `gatling`) e `{ext}` (ex: `.js`, `.scala`).

---

#### Pergunta B — Tipos de teste

**AskUserQuestion** (multi select):
> "Quais tipos de teste deseja implementar para esta feature?"

| Opção | Escopo |
|---|---|
| **E2E Funcional** | Um script por CT — valida contrato e regras de negócio |
| **Teste de Carga (Load)** | Scripts de throughput por estágio de RPS — apenas CTs de sucesso |
| **Chaos Engineering** | Scripts de resiliência sob falha — apenas CTs de sucesso |

Registre os tipos selecionados — eles controlam quais seções e arquivos são gerados.

---

#### Pergunta C — Especificação técnica detalhada

**AskUserQuestion** (open-ended):
> "Descreva tecnicamente o que deseja testar. Preencha o JSON abaixo com o que souber — quanto mais detalhe, mais precisa a especificação gerada."

Apresente o template:

```json
{
  "request": {
    "httpMethod": "POST",
    "path": "/resource",
    "headers": {},
    "requestBody": {}
  },
  "rules": [
    { "description": "Regra de negócio ou comportamento esperado" },
    { "classes": { "entity": ["field1", "field2"] }, "description": "Campos que devem ser persistidos" },
    { "sequenceDescription": "Sequência de operações que o sistema deve executar" }
  ],
  "response": {
    "status": 201,
    "headers": {
      "Location": "/resource/{id}"
    },
    "body": {}
  },
  "database": {
    "table": "table_name",
    "assertedFields": ["field1", "field2"]
  },
  "loadProfile": {
    "stages": [
      { "rps": 100, "duration": "5m" },
      { "rps": 1000, "duration": "5m" }
    ]
  },
  "chaosScenarios": [
    { "type": "shutdown-pods", "description": "Desliga pods durante o teste" }
  ]
}
```

---

### 5. Análise profunda — extrair detalhes técnicos

Com `tests.md` + respostas do questionário, analise **antes de gerar qualquer arquivo**:

**Do `request`:**
- Quais campos são obrigatórios vs opcionais?
- Há tipos especiais: UUID, enum, nested object, null explícito?
- Há headers necessários: Authorization, Content-Type, X-Correlation-Id?

**Das `rules`:**
- Quais rules implicam asserções de banco (persistência, lock, update parcial)?
- Há `sequenceDescription`? → gere critério de ordem de operações nos CTs correspondentes
- Há campos cujo valor no banco difere do enviado na request (ex: campo calculado)?

**Da `response`:**
- Há `Location` header? → todo script de sucesso deve extrair o ID e usá-lo em asserções
- O body tem campos derivados (código gerado, timestamps)? → defina como validá-los
- Qual é o código HTTP exato para cada CT de rejeição?

**Do `loadProfile`:**
- VUs estimados por estágio: `VUs ≈ RPS × latência_média_em_segundos` (use 0.2s como default se desconhecida)
- Há necessidade de warmup antes do platô?

**Do `chaosScenarios`:**
- O mecanismo de caos requer kubectl, toxiproxy ou outra ferramenta?

Se qualquer ponto estiver ambíguo, **faça uma pergunta de esclarecimento** antes de prosseguir.

---

### 6. Verificar existência de `testspec/{feature-name}/`

Verifique se `./testspec/{feature-name}/` já existe no diretório atual.

Se existir, pergunte via **AskUserQuestion** (single select):
- **Sobrescrever** — apaga e recria os arquivos
- **Cancelar** — encerra sem alterações

---

### 7. Criar estrutura de diretórios

Crie **todos** os diretórios abaixo relativos ao diretório atual (`.`):

```
./testspec/{feature-name}/

./src/test/features/{feature-name}/e2e/
./src/test/features/{feature-name}/load/              ← somente se Load selecionado
./src/test/features/{feature-name}/chaos-engineering/ ← somente se Chaos selecionado
```

---

### 8. Gerar `./testspec/{feature-name}/spec.qa.md`

Siga o **Formato spec.qa.md** abaixo.

### 9. Gerar `./testspec/{feature-name}/tasks.qa.md`

Siga o **Formato tasks.qa.md** abaixo.

---

### 10. Exibir confirmação final

```
Especificação QA gerada para '{feature-name}':

  Ferramenta:    {K6 | Gatling | Outro}
  Testes:        {E2E} {Load} {Chaos}

  Artefatos criados em ./testspec/{feature-name}/
    spec.qa.md
    tasks.qa.md

  Estrutura de pastas criada em ./src/test/features/{feature-name}/
    e2e/          ({N} scripts mapeados)
    load/         ({N} scripts mapeados)     ← omitir se não selecionado
    chaos-engineering/ ({N} scripts)         ← omitir se não selecionado

Próximo passo: /testspec-apply-qa para implementar os scripts.
```

---

## Formato spec.qa.md

```markdown
# Tests Spec — {Nome Legível da Feature}

> Especificação técnica dos scripts de teste derivada do tests.md + questionário QA.
> Ferramenta: {K6 | Gatling | Outro}

---

## Contexto

{Resumo da feature: protocolo de entrada, operação, regras de negócio relevantes para os testes. 3-5 linhas.}

---

## Contrato Técnico

### Request

```
{HTTP_METHOD} {path}
Content-Type: application/json
{outros headers relevantes com exemplo de valor}

Body:
{
  "{campo}": <Tipo>,   -- {obrigatório|opcional} — {descrição}
  ...
}
```

### Response (Sucesso)

```
HTTP Status: {código}
Location: {path}/{id}   ← incluir somente se presente

Body:
{
  "{campo}": <Tipo>,   -- {descrição}
}
```

### Regras de Negócio para Testes

{Derivadas das `rules` do questionário e dos critérios do tests.md}
- {regra 1}
- {regra 2}
- {regra N}

---

## Cobertura de Testes

### E2E

| CT    | Arquivo                                       | Cenário             | Tipo     |
|-------|-----------------------------------------------|---------------------|----------|
| CT-01 | {tool}-e2e-{acao}-{cenario}{ext}              | {nome do cenário}   | Sucesso  |
| CT-NN | {tool}-e2e-{acao}-{cenario}{ext}              | {nome do cenário}   | Rejeição |

### Load   ← incluir somente se Load selecionado

| Cenário | Arquivo                                              | VUs est. | Duração | RPS alvo |
|---------|------------------------------------------------------|----------|---------|----------|
| CT-01   | {tool}-load-{acao}-{cenario}-100-rps-5min{ext}       | ~20      | 5min    | 100      |
| CT-01   | {tool}-load-{acao}-{cenario}-1000-rps-5min{ext}      | ~200     | 5min    | 1000     |

### Chaos Engineering   ← incluir somente se Chaos selecionado

| Cenário | Arquivo                                              | Tipo de caos  | Mecanismo     |
|---------|------------------------------------------------------|---------------|---------------|
| CT-01   | {tool}-dr-{acao}-{cenario}-shutdown-pods{ext}        | Pod shutdown  | kubectl delete|

---

## Estrutura de Pastas

```
src/test/features/{feature-name}/
  e2e/
    {tool}-e2e-{acao}-{cenario}{ext}
    {tool}-e2e-{acao}-{cenario}.md    ← run plan gerado pelo /testspec-apply-qa
  load/                               ← somente se Load selecionado
    {tool}-load-{acao}-{cenario}-{rps}-rps-{duracao}{ext}
    {tool}-load-{acao}-{cenario}-{rps}-rps-{duracao}.md  ← run plan
  chaos-engineering/                  ← somente se Chaos selecionado
    {tool}-dr-{acao}-{cenario}-{tipo-caos}{ext}
    {tool}-dr-{acao}-{cenario}-{tipo-caos}.md            ← run plan
```

## Convenção de Nomenclatura

| Tipo  | Padrão                                              |
|-------|-----------------------------------------------------|
| E2E   | `{tool}-e2e-{acao}-{cenario}{ext}`                  |
| Load  | `{tool}-load-{acao}-{cenario}-{rps}-rps-{dur}{ext}` |
| Chaos | `{tool}-dr-{acao}-{cenario}-{tipo-caos}{ext}`       |

Onde:
- `{acao}`: verbo da operação em kebab-case (ex: `create-order`, `consume-message`)
- `{cenario}`: resultado esperado em kebab-case (ex: `success`, `bad-request-zero-value`)
- `{rps}`: número inteiro de requisições por segundo alvo
- `{dur}`: duração do estágio (ex: `5min`, `10min`)
- `{tipo-caos}`: tipo de falha em kebab-case (ex: `shutdown-pods`, `network-latency`)

---

## Casos de Teste — Mapeamento Detalhado

### CT-01 — {Nome do Cenário}

**Arquivo E2E:** `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}{ext}`
**Tipo:** Sucesso

**Entrada:**
```json
{payload completo extraído do tests.md e do questionário, com valores de exemplo reais}
```

**Critérios de Aceite para o script:**
- Verificar status HTTP {código}
- {Se Location header presente}: Extrair `{id}` do header `Location` e usar em asserções subsequentes
- Verificar campo `{campo}` no body de resposta igual a `{valor}`
- {Se persistência}: Consultar tabela `{tabela}` e verificar `{campo}` = `{valor}` após a operação
- {Se sequenceDescription}: Verificar que `{operação A}` ocorre antes de `{operação B}`

---

### CT-NN — {Nome do Cenário}

**Arquivo E2E:** `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}{ext}`
**Tipo:** Rejeição

**Entrada:**
```json
{payload inválido com o campo problemático destacado}
```

**Critérios de Aceite para o script:**
- Verificar status HTTP {código de erro — ex: 400, 422}
- Verificar que COUNT na tabela `{tabela}` permanece igual ao valor anterior à requisição
- {Se body de erro padronizado}: Verificar estrutura do body de erro
```

---

## Formato tasks.qa.md

```markdown
# Tasks — QA {Nome Legível da Feature}

> Tarefas de implementação derivadas do spec.qa.md.
> Ferramenta: {K6 | Gatling | Outro}

---

## 1. E2E Tests

- [ ] 1.1 Criar `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}{ext}` — CT-01: {nome}
- [ ] 1.2 Criar `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}.md` — run plan CT-01
- [ ] 1.N Criar `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}{ext}` — CT-NN: {nome}
- [ ] 1.N+1 Criar `src/test/features/{feature-name}/e2e/{tool}-e2e-{...}.md` — run plan CT-NN

## 2. Load Tests   ← incluir somente se Load selecionado

- [ ] 2.1 Criar `src/test/features/{feature-name}/load/{tool}-load-{...}-100-rps-5min{ext}` — CT-01, 100 RPS
- [ ] 2.2 Criar `src/test/features/{feature-name}/load/{tool}-load-{...}-100-rps-5min.md` — run plan
- [ ] 2.3 Criar `src/test/features/{feature-name}/load/{tool}-load-{...}-1000-rps-5min{ext}` — CT-01, 1000 RPS
- [ ] 2.4 Criar `src/test/features/{feature-name}/load/{tool}-load-{...}-1000-rps-5min.md` — run plan

## 3. Chaos Engineering   ← incluir somente se Chaos selecionado

- [ ] 3.1 Criar `src/test/features/{feature-name}/chaos-engineering/{tool}-dr-{...}-shutdown-pods{ext}` — CT-01, pod failure
- [ ] 3.2 Criar `src/test/features/{feature-name}/chaos-engineering/{tool}-dr-{...}-shutdown-pods.md` — run plan

## 4. Verificação

- [ ] 4.1 Validar sintaxe de todos os scripts E2E (dry-run ou lint da ferramenta escolhida)
- [ ] 4.2 Revisar thresholds de load com SRE/produto antes de executar
- [ ] 4.3 Confirmar mecanismo de caos disponível no ambiente (kubectl, toxiproxy, etc.)
```

---

## Guardrails

- **Nunca peça ao usuário para criar arquivos ou diretórios** — se `./testspec/`, `instructions.md` ou `current-feature.md` não existirem, crie-os imediatamente com conteúdo padrão e prossiga
- **`instructions.md` criado automaticamente** com valores opinativos sensatos (`app_repo_local_path: ../development-flow-sdd-sdt`, k6 como ferramenta padrão, thresholds default)
- **`instructions.md` tem precedência** sobre qualquer default da skill após criado — se o usuário personalizou, siga o arquivo
- **`current-feature.md` elimina a pergunta de seleção** — se contiver um nome de feature, use-o diretamente sem perguntar ao usuário
- **Diretório de saída:** sempre `./testspec/` e `./src/test/features/` relativos ao diretório atual — nunca use paths absolutos ou hardcoded
- **GitHub MCP é a única fonte do app repo** — nunca use caminhos relativos ou locais (`../`) para acessar o repositório do app
- **`app_repo` em `instructions.md`** define o `owner/repo` usado em todas as chamadas GitHub MCP — sempre leia dali, nunca hardcode
- **Seleção de feature obrigatória quando `current-feature.md` está vazio:** sempre listar features do GitHub MCP e perguntar — nunca assumir
- **Questionário obrigatório:** não gere nenhum arquivo antes de concluir as Perguntas A, B e C (spec técnica)
- **Nomenclatura:** sempre kebab-case, sem acentos, sem espaços, sem caracteres especiais
- **`tasks.qa.md`:** usa exclusivamente `- [ ]` — nunca bullets simples
- **Load e Chaos:** SOMENTE para CTs de sucesso (happy path) — CTs de rejeição ficam apenas em E2E
- **Seções condicionais:** omitir Load e Chaos inteiramente se não foram selecionados em Pergunta B
- **Protocolo Kafka:** se o protocolo for Kafka (não HTTP), omitir seções HTTP, adaptar `{acao}` para `consume-{recurso}`, omitir `Location` header
- **Location header:** se presente na resposta, SEMPRE incluir instrução de extração do ID nos critérios de aceite do CT de sucesso
- **sequenceDescription:** se presente nas rules, SEMPRE gerar critério de ordem de operações no CT correspondente
- **Próximo passo:** sempre referenciar `/testspec-apply-qa`
- **Run plans:** sempre gerados pelo `/testspec-apply-qa` junto com cada script — não pergunte ao usuário, não condicione
