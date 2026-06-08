## ADDED Requirements

### Requirement: Conta entity maps the conta table
The `Conta` JPA entity SHALL map columns: `id_conta` (PK auto-increment), `id_cliente` (FK → cliente, NOT NULL), `numero` (VARCHAR 20, unique), `tipo_conta` (enum: PF, PJ), `saldo` (DECIMAL 15,2, default 0), `status` (enum: ATIVA, BLOQUEADA, ENCERRADA, default ATIVA). The entity SHALL have a `@ManyToOne` association to `Cliente`.

#### Scenario: Conta persisted with valid cliente reference
- **WHEN** a `Conta` with a valid id_cliente, numero, and tipo_conta is saved
- **THEN** the record appears in the `conta` table with a generated `id_conta`

### Requirement: POST /api/contas creates a new conta
The endpoint SHALL accept a JSON body with `idCliente`, `numero`, `tipoConta`, `saldo`, `status` and return HTTP 201.

#### Scenario: Successful creation
- **WHEN** POST /api/contas is called with an existing idCliente and unique numero
- **THEN** the response is HTTP 201 with the new conta

#### Scenario: Non-existent cliente returns not found
- **WHEN** POST /api/contas is called with a non-existent idCliente
- **THEN** the response is HTTP 404

### Requirement: GET /api/contas returns all contas
#### Scenario: List all
- **WHEN** GET /api/contas is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/contas/{id} returns a single conta
#### Scenario: Found
- **WHEN** GET /api/contas/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the conta JSON

#### Scenario: Not found
- **WHEN** GET /api/contas/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/contas/{id} updates a conta
#### Scenario: Successful update of status
- **WHEN** PUT /api/contas/{id} is called with status=BLOQUEADA
- **THEN** the response is HTTP 200 with updated status

### Requirement: DELETE /api/contas/{id} removes a conta
#### Scenario: Successful deletion
- **WHEN** DELETE /api/contas/{id} is called
- **THEN** the response is HTTP 204 and the record is removed
