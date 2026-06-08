## ADDED Requirements

### Requirement: Cliente entity maps the cliente table
The `Cliente` JPA entity SHALL map columns: `id_cliente` (PK auto-increment), `cpf` (CHAR 11, unique), `cnpj` (CHAR 14, nullable, unique), `nome` (VARCHAR 150), `email` (VARCHAR 150, unique), `segmento` (enum: MEDICO, DENTISTA, FISIOTERAPEUTA, OUTRO), `criado_em` (DATETIME, default now).

#### Scenario: Entity is persisted with all required fields
- **WHEN** a `Cliente` with valid cpf, nome, email, and segmento is saved
- **THEN** the record appears in the `cliente` table with a generated `id_cliente`

### Requirement: POST /api/clientes creates a new cliente
The endpoint SHALL accept a JSON body and return HTTP 201 with the created resource.

#### Scenario: Successful creation
- **WHEN** POST /api/clientes is called with valid cpf, nome, email, segmento
- **THEN** the response is HTTP 201 with the new cliente including generated id_cliente

#### Scenario: Duplicate cpf returns conflict
- **WHEN** POST /api/clientes is called with a cpf that already exists
- **THEN** the response is HTTP 409

### Requirement: GET /api/clientes returns all clientes
The endpoint SHALL return HTTP 200 with a JSON array of all clientes.

#### Scenario: List all
- **WHEN** GET /api/clientes is called
- **THEN** the response is HTTP 200 with a JSON array

### Requirement: GET /api/clientes/{id} returns a single cliente
#### Scenario: Found
- **WHEN** GET /api/clientes/{id} is called with a valid id
- **THEN** the response is HTTP 200 with the cliente JSON

#### Scenario: Not found
- **WHEN** GET /api/clientes/{id} is called with a non-existent id
- **THEN** the response is HTTP 404

### Requirement: PUT /api/clientes/{id} updates a cliente
#### Scenario: Successful update
- **WHEN** PUT /api/clientes/{id} is called with updated fields
- **THEN** the response is HTTP 200 with the updated cliente

### Requirement: DELETE /api/clientes/{id} removes a cliente
#### Scenario: Successful deletion
- **WHEN** DELETE /api/clientes/{id} is called with a valid id
- **THEN** the response is HTTP 204 and the record is removed
