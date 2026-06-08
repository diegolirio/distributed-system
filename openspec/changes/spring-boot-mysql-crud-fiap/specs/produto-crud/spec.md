## ADDED Requirements

### Requirement: Produto entity maps the produto table
The `Produto` JPA entity SHALL map columns: `id_produto` (PK auto-increment), `nome` (VARCHAR 120, unique), `categoria` (enum: CREDITO, INVESTIMENTO, SAAS, SERVICO), `taxa_juros` (DECIMAL 7,4, default 0), `ativo` (TINYINT 1, default 1).

#### Scenario: Entity persisted with minimum required fields
- **WHEN** a `Produto` with nome, categoria, and taxa_juros ≥ 0 is saved
- **THEN** the record appears in the `produto` table with a generated `id_produto`

### Requirement: POST /api/produtos creates a new produto
The endpoint SHALL accept a JSON body and return HTTP 201.

#### Scenario: Successful creation
- **WHEN** POST /api/produtos is called with valid nome, categoria, taxa_juros
- **THEN** the response is HTTP 201 with the new produto including generated id_produto

#### Scenario: Negative taxa_juros returns bad request
- **WHEN** POST /api/produtos is called with taxa_juros < 0
- **THEN** the response is HTTP 400

### Requirement: GET /api/produtos returns all produtos
#### Scenario: List all
- **WHEN** GET /api/produtos is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/produtos/{id} returns a single produto
#### Scenario: Found
- **WHEN** GET /api/produtos/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the produto JSON

#### Scenario: Not found
- **WHEN** GET /api/produtos/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/produtos/{id} updates a produto
#### Scenario: Successful update
- **WHEN** PUT /api/produtos/{id} is called with updated fields
- **THEN** the response is HTTP 200 with the updated produto

### Requirement: DELETE /api/produtos/{id} removes a produto
#### Scenario: Successful deletion
- **WHEN** DELETE /api/produtos/{id} is called with a valid id
- **THEN** the response is HTTP 204 and the record is removed
