## ADDED Requirements

### Requirement: TipoTransacao entity maps the tipo_transacao table
The `TipoTransacao` JPA entity SHALL map columns: `id_tipo` (PK auto-increment), `descricao` (VARCHAR 60, unique), `sinal` (TINYINT, must be -1 or 1).

#### Scenario: Entity persisted with valid sinal
- **WHEN** a `TipoTransacao` with descricao and sinal = 1 or -1 is saved
- **THEN** the record appears in the `tipo_transacao` table

### Requirement: POST /api/tipos-transacao creates a new tipo
The endpoint SHALL accept a JSON body and return HTTP 201.

#### Scenario: Successful creation with sinal 1 (credit)
- **WHEN** POST /api/tipos-transacao is called with descricao and sinal=1
- **THEN** the response is HTTP 201 with the new tipo including generated id_tipo

#### Scenario: Invalid sinal returns bad request
- **WHEN** POST /api/tipos-transacao is called with sinal=0
- **THEN** the response is HTTP 400

### Requirement: GET /api/tipos-transacao returns all tipos
#### Scenario: List all
- **WHEN** GET /api/tipos-transacao is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/tipos-transacao/{id} returns a single tipo
#### Scenario: Found
- **WHEN** GET /api/tipos-transacao/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the tipo JSON

#### Scenario: Not found
- **WHEN** GET /api/tipos-transacao/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/tipos-transacao/{id} updates a tipo
#### Scenario: Successful update
- **WHEN** PUT /api/tipos-transacao/{id} is called with updated descricao or sinal
- **THEN** the response is HTTP 200 with the updated tipo

### Requirement: DELETE /api/tipos-transacao/{id} removes a tipo
#### Scenario: Successful deletion
- **WHEN** DELETE /api/tipos-transacao/{id} is called
- **THEN** the response is HTTP 204 and the record is removed
