## ADDED Requirements

### Requirement: Transacao entity maps the transacao table
The `Transacao` JPA entity SHALL map columns: `id_transacao` (BIGINT PK auto-increment), `id_conta` (FK → conta), `id_produto` (FK → produto), `id_tipo` (FK → tipo_transacao), `id_contratacao` (FK → contratacao, nullable), `valor` (DECIMAL 15,2, must be > 0), `data_hora` (DATETIME, default now), `id_idempotencia` (CHAR 36 UUID, unique). The entity SHALL have `@ManyToOne` associations for all FK columns.

#### Scenario: Transacao persisted with valid references and positive valor
- **WHEN** a `Transacao` with valid FK ids, valor > 0, and unique id_idempotencia is saved
- **THEN** the record appears in the `transacao` table with a generated `id_transacao`

### Requirement: POST /api/transacoes creates a new transacao
The endpoint SHALL accept `idConta`, `idProduto`, `idTipo`, `idContratacao` (optional), `valor`, `idIdempotencia` and return HTTP 201.

#### Scenario: Successful creation
- **WHEN** POST /api/transacoes is called with valid references and valor > 0
- **THEN** the response is HTTP 201 with the new transacao

#### Scenario: valor ≤ 0 returns bad request
- **WHEN** POST /api/transacoes is called with valor = 0 or negative
- **THEN** the response is HTTP 400

#### Scenario: Duplicate idIdempotencia returns conflict
- **WHEN** POST /api/transacoes is called with an idIdempotencia already present in the table
- **THEN** the response is HTTP 409

### Requirement: GET /api/transacoes returns all transacoes
#### Scenario: List all
- **WHEN** GET /api/transacoes is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/transacoes/{id} returns a single transacao
#### Scenario: Found
- **WHEN** GET /api/transacoes/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the transacao JSON

#### Scenario: Not found
- **WHEN** GET /api/transacoes/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/transacoes/{id} updates a transacao
#### Scenario: Successful update of valor
- **WHEN** PUT /api/transacoes/{id} is called with a new valid valor
- **THEN** the response is HTTP 200 with updated transacao

### Requirement: DELETE /api/transacoes/{id} removes a transacao
#### Scenario: Successful deletion
- **WHEN** DELETE /api/transacoes/{id} is called
- **THEN** the response is HTTP 204 and the record is removed
