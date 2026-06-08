## ADDED Requirements

### Requirement: Contratacao entity maps the contratacao table
The `Contratacao` JPA entity SHALL map columns: `id_contratacao` (PK auto-increment), `id_cliente` (FK → cliente), `id_produto` (FK → produto), `data_contratacao` (DATE), `status` (enum: ATIVA, SUSPENSA, LIQUIDADA, CANCELADA, default ATIVA). The entity SHALL have `@ManyToOne` associations to `Cliente` and `Produto`. A unique constraint SHALL exist on (id_cliente, id_produto, data_contratacao).

#### Scenario: Contratacao persisted with valid references
- **WHEN** a `Contratacao` with existing idCliente, idProduto, and data_contratacao is saved
- **THEN** the record appears in the `contratacao` table with a generated `id_contratacao`

### Requirement: POST /api/contratacoes creates a new contratacao
The endpoint SHALL accept `idCliente`, `idProduto`, `dataContratacao`, `status` and return HTTP 201.

#### Scenario: Successful creation
- **WHEN** POST /api/contratacoes is called with valid idCliente, idProduto, and dataContratacao
- **THEN** the response is HTTP 201 with the new contratacao

#### Scenario: Duplicate (cliente + produto + data) returns conflict
- **WHEN** POST /api/contratacoes is called with the same idCliente, idProduto, dataContratacao as an existing record
- **THEN** the response is HTTP 409

### Requirement: GET /api/contratacoes returns all contratacoes
#### Scenario: List all
- **WHEN** GET /api/contratacoes is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/contratacoes/{id} returns a single contratacao
#### Scenario: Found
- **WHEN** GET /api/contratacoes/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the contratacao JSON

#### Scenario: Not found
- **WHEN** GET /api/contratacoes/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/contratacoes/{id} updates a contratacao
#### Scenario: Status update to LIQUIDADA
- **WHEN** PUT /api/contratacoes/{id} is called with status=LIQUIDADA
- **THEN** the response is HTTP 200 with updated status

### Requirement: DELETE /api/contratacoes/{id} removes a contratacao
#### Scenario: Successful deletion
- **WHEN** DELETE /api/contratacoes/{id} is called
- **THEN** the response is HTTP 204 and the record is removed
