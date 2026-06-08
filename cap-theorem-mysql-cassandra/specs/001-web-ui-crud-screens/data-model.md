# Phase 1 Data Model: Web UI — Home, Menu & CRUD Screens

This feature introduces **no new persisted entities**. It reuses the six existing JPA entities and their MySQL tables (Flyway `V1`–`V6`). What is new at the model layer are the **form-backing beans** (one per area) used for HTML binding/validation, and the **list/detail view shapes** rendered by templates. Field rules below are derived from the migration constraints and the spec's Functional Requirements.

## Existing domain entities (reused, unchanged)

| Area | Entity / Table | Identity | References (FK) | Referenced by |
|------|----------------|----------|-----------------|---------------|
| Tipos de Transação | `TipoTransacao` / `tipo_transacao` | `idTipo` | — | Transação |
| Clientes | `Cliente` / `cliente` | `idCliente` | — | Conta, Contratação |
| Produtos | `Produto` / `produto` | `idProduto` | — | Contratação, Transação |
| Contas | `Conta` / `conta` | `idConta` | Cliente | Transação |
| Contratações | `Contratacao` / `contratacao` | `idContratacao` | Cliente, Produto | Transação (optional) |
| Transações | `Transacao` / `transacao` | `idTransacao` | Conta, Produto, TipoTransacao, Contratação? | — |

## Form-backing beans (new, presentation only)

Validation annotations reflect the DB constraints. On the create/edit forms these drive `th:errors` field-level messages (FR-009). Enum fields render as constrained `<select>` (FR-016); FK fields render as `<select>` populated from the referenced area (FR-011).

### TipoTransacaoForm
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| descricao | String | `@NotBlank`, `@Size(max=60)`, unique (DB) | text |
| sinal | Int | `@NotNull`, value ∈ {-1, +1} | select (Crédito +1 / Débito -1) |

### ClienteForm
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| cpf | String | `@NotBlank`, `@Pattern(\d{11})`, unique (DB) | text |
| cnpj | String? | optional, `@Pattern(\d{14})` when present, unique (DB) | text |
| nome | String | `@NotBlank`, `@Size(max=150)` | text |
| email | String | `@NotBlank`, `@Email`, `@Size(max=150)`, unique (DB) | email |
| segmento | enum `SegmentoCliente` | `@NotNull` | select (MEDICO/DENTISTA/FISIOTERAPEUTA/OUTRO) |
| _criadoEm_ | — | system-set on create, read-only | (display only) |

### ProdutoForm
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| nome | String | `@NotBlank`, `@Size(max=120)`, unique (DB) | text |
| categoria | enum `CategoriaProduto` | `@NotNull` | select (CREDITO/INVESTIMENTO/SAAS/SERVICO) |
| taxaJuros | BigDecimal | `@NotNull`, `@DecimalMin("0.0")`, scale ≤ 4 | number |
| ativo | Boolean | default true | checkbox |

### ContaForm
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| idCliente | Int | `@NotNull` (FK → Cliente) | select (Cliente nome + CPF) |
| numero | String | `@NotBlank`, `@Size(max=20)`, unique (DB) | text |
| tipoConta | enum `TipoConta` | `@NotNull` | select (PF/PJ) |
| saldo | BigDecimal | `@NotNull`, `@DecimalMin("-1000000.00")` | number |
| status | enum `StatusConta` | `@NotNull` | select (ATIVA/BLOQUEADA/ENCERRADA) |

### ContratacaoForm
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| idCliente | Int | `@NotNull` (FK → Cliente) | select |
| idProduto | Int | `@NotNull` (FK → Produto) | select |
| dataContratacao | LocalDate | `@NotNull` | date |
| status | enum `StatusContratacao` | `@NotNull` | select (ATIVA/SUSPENSA/LIQUIDADA/CANCELADA) |
| _unique_ | — | (idCliente, idProduto, dataContratacao) unique (DB) → conflict message | — |

### TransacaoForm  *(create only — no edit; see research §7)*
| Field | Type | Validation | Control |
|-------|------|-----------|---------|
| idConta | Int | `@NotNull` (FK → Conta) | select |
| idProduto | Int | `@NotNull` (FK → Produto) | select |
| idTipo | Int | `@NotNull` (FK → TipoTransacao) | select |
| idContratacao | Int? | optional (FK → Contratação) | select (com "—" / nenhuma) |
| valor | BigDecimal | `@NotNull`, `@DecimalMin(value="0", inclusive=false)` (> 0) | number |
| idIdempotencia | String | `@NotBlank`, length 36 (UUID), unique (DB) | text (or auto-generated) |
| _dataHora_ | — | system-set default CURRENT_TIMESTAMP, read-only | (display only) |

## List view shapes (key columns per FR-004)

| Area | Listing columns |
|------|-----------------|
| Tipos de Transação | descrição, sinal |
| Clientes | nome, CPF, e-mail, segmento |
| Produtos | nome, categoria, taxa de juros, ativo |
| Contas | número, cliente (nome), tipo, saldo, status |
| Contratações | cliente (nome), produto (nome), data, status |
| Transações | data/hora, conta (número), tipo, valor, produto |

## Validation → error mapping (FR-012, FR-013, FR-015)

| Source | Trigger | UI outcome |
|--------|---------|-----------|
| Bean Validation (`@Valid`) | invalid/missing field | re-render form fragment with `th:errors` beside field |
| `DataIntegrityViolationException` (unique) | duplicate cpf/cnpj/email/numero/nome/idempotência or (cliente,produto,data) | alert fragment: campo já existe |
| `DataIntegrityViolationException` (FK restrict) | delete a referenced record | alert fragment: existem registros vinculados |
| `EntityNotFoundException` | open/edit/delete missing id | "registro não encontrado" fragment |

## State transitions

No formal state machine is enforced by this feature. Enum status fields (`Conta.status`, `Contratacao.status`) are freely editable through the edit form; any business rules around transitions are out of scope and remain as currently implemented in the service layer.
