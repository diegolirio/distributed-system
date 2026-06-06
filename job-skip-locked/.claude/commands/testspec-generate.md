---
name: testspec-generate
description: Generate tests.md from SDD spec artifacts (OpenSpec), consolidating all spec.md files and enriching with technical acceptance criteria.
license: MIT
compatibility: Requires @analizza-ai/testspec and OpenSpec layout.
metadata:
  author: analizza-ai
  version: "0.1.2"
  generatedBy: testspec
---

# testspec-generate

Gera o arquivo `tests.md` para uma feature do openspec, consolidando todos os `spec.md` da feature e enriquecendo com critérios de aceite técnicos e genéricos de tecnologia.

## Quando usar

Após `/opsx:propose` — quando todos os `spec.md` da feature estiverem criados e o `design.md` existir.

## Instructions

### 1. Identificar a feature

- Se um argumento foi passado (nome ou caminho da feature), use-o diretamente.
- Caso contrário, liste os diretórios em `openspec/changes/` e pergunte ao usuário qual feature processar usando **AskUserQuestion**.

### 2. Ler os arquivos-base

Leia **todos** os arquivos nesta ordem:
1. `openspec/changes/<feature>/design.md`
2. Todos os arquivos `openspec/changes/<feature>/specs/**/spec.md` (pode ser mais de um)

### 3. Gerar o `tests.md`

Crie o arquivo em `openspec/changes/<feature>/tests.md` seguindo rigorosamente o **Formato de Saída** abaixo.

**Regras de geração:**
- Escrito inteiramente em **português**
- **Tecnologia-agnóstico**: use termos universais — `HTTP Method`, `HTTP Status Code`, `URL`, `Content-Type`, campos JSON com tipo explícito (`<String>`, `<Decimal>`, `<Long>`, `<Boolean>`)
- Para cada `### Requirement` nos spec.md, gere um ou mais casos de teste (`CT-NN`)
- Para cada `#### Scenario` nos spec.md, gere exatamente um caso de teste
- Enriqueça cada caso de teste com critérios de aceite além do que o cenário descreve: validações de status HTTP, campos do body da resposta, estado no banco de dados, etc.
- A seção **Validações de Banco de Dados** só aparece quando o design.md descreve persistência
- A seção **Fora de Escopo** deve ser derivada dos Non-Goals do `proposal.md` (se disponível) ou do `design.md`

### 4. Confirmar criação

Exiba ao usuário:
```
tests.md criado em openspec/changes/<feature>/tests.md
  Casos de teste: <N>
  Specs consolidadas: <lista dos spec.md lidos>
```

---

## Formato de Saída (`tests.md`)

```markdown
# Testes — <Nome Legível da Feature>

> Documento de especificação de testes de integração e E2E.
> Tecnologia-agnóstico: use HTTP padrão para qualquer linguagem ou ferramenta.

---

## Contexto

<Resumo do que a feature faz, derivado do design.md. 2-4 linhas.>

**Recurso:** `<path>`
**Método HTTP:** `<METHOD>`   ← omitir se não for HTTP
**Banco de dados:** `<tabela>` ← omitir se não houver persistência

---

## Estrutura da Requisição   ← omitir se não for HTTP

\`\`\`
HTTP Method : <METHOD>
URL         : <path>
Content-Type: application/json

Body:
{
  "<campo>": <Tipo>,  -- descrição
}
\`\`\`

## Estrutura da Resposta (sucesso)   ← omitir se não houver body de resposta

\`\`\`
HTTP Status : <código>
Content-Type: application/json

Body:
{
  "<campo>": <Tipo>,
}
\`\`\`

---

## Casos de Teste

### CT-01 — <Nome do cenário>

**Descrição:** <o que este teste valida>

**Pré-condição:** <estado necessário antes de executar>

**Entrada:**
\`\`\`
<representação tecnologia-agnóstica da entrada>
\`\`\`

**Critérios de Aceite:**
- <critério 1>
- <critério 2>
- ...

---

### CT-NN — ...

---

## Validações de Banco de Dados   ← incluir somente se houver persistência

<tabela com colunas e valores esperados para o cenário de sucesso>
<instrução de COUNT para cenários de rejeição>

---

## Fora de Escopo

- <item 1>
- <item 2>
```

---

## Guardrails

- Nunca copiar blocos `<context>` ou `<rules>` do openspec para o arquivo gerado
- Nunca usar nomes de classes, anotações ou frameworks no `tests.md` (ex: `@SpringBootTest`, `BigDecimal`, `KafkaContainer`)
- Sempre numerar casos de teste sequencialmente: CT-01, CT-02, ...
- Se houver múltiplos `spec.md`, consolidar todos no mesmo `tests.md` agrupando por capability
- Se `tests.md` já existir, perguntar ao usuário se deseja sobrescrever ou atualizar
