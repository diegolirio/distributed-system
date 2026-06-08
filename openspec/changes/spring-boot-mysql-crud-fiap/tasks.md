## 1. Module Scaffold

- [x] 1.1 Create directory `cap-theorem-mysql-cassandra/cap-theorem-mysql-cassandra/`
- [x] 1.2 Create `build.gradle.kts` with plugins: kotlin jvm, kotlin spring, kotlin jpa, spring boot, dependency-management; add all required dependencies (JPA, web, flyway, flyway-mysql, mysql-connector-j, testcontainers, flyway-test)
- [x] 1.3 Create `settings.gradle.kts` for the new module (or register it in the root settings if a multi-module root exists)
- [x] 1.4 Create `src/main/kotlin/ai/analizza/cap/theorem/mysql/cassandra/CapTheoremMysqlCassandraApplication.kt` main entry point
- [x] 1.5 Create `src/main/resources/application.yml` with datasource pointing to `fiapdb`, hikari pool config, jpa ddl-auto=validate, flyway enabled
- [x] 1.6 Copy `docker-compose.mysql.yml` from FIAP assets into the new module root

## 2. Flyway Migrations

- [x] 2.1 Create `src/main/resources/db/migration/V1__create_tipo_transacao.sql` — DDL for `tipo_transacao` table with unique constraint on descricao and CHECK on sinal
- [x] 2.2 Create `V2__create_cliente.sql` — DDL for `cliente` table with unique constraints on cpf, cnpj, email and ENUM segmento
- [x] 2.3 Create `V3__create_produto.sql` — DDL for `produto` table with unique nome and ENUM categoria
- [x] 2.4 Create `V4__create_conta.sql` — DDL for `conta` table with FK → cliente, ENUM tipo_conta and status
- [x] 2.5 Create `V5__create_contratacao.sql` — DDL for `contratacao` table with FK → cliente and produto, composite unique (id_cliente, id_produto, data_contratacao)
- [x] 2.6 Create `V6__create_transacao.sql` — DDL for `transacao` table with FKs → conta, produto, tipo_transacao, contratacao (nullable), and unique id_idempotencia; include operational indexes

## 3. Entities

- [x] 3.1 Create `entity/TipoTransacao.kt` — `@Entity @Table("tipo_transacao")` with all columns; sinal as Int
- [x] 3.2 Create `entity/Cliente.kt` — `@Entity @Table("cliente")` with segmento as Kotlin enum
- [x] 3.3 Create `entity/Produto.kt` — `@Entity @Table("produto")` with categoria as Kotlin enum; ativo as Boolean
- [x] 3.4 Create `entity/Conta.kt` — `@Entity @Table("conta")` with `@ManyToOne` to Cliente; tipoConta and status as Kotlin enums
- [x] 3.5 Create `entity/Contratacao.kt` — `@Entity @Table("contratacao")` with `@ManyToOne` to Cliente and Produto; status as Kotlin enum
- [x] 3.6 Create `entity/Transacao.kt` — `@Entity @Table("transacao")` with `@ManyToOne` to Conta, Produto, TipoTransacao, Contratacao (nullable); valor as BigDecimal; idIdempotencia as String

## 4. Repositories

- [x] 4.1 Create `repository/TipoTransacaoRepository.kt` extending `JpaRepository<TipoTransacao, Int>`
- [x] 4.2 Create `repository/ClienteRepository.kt` extending `JpaRepository<Cliente, Int>`
- [x] 4.3 Create `repository/ProdutoRepository.kt` extending `JpaRepository<Produto, Int>`
- [x] 4.4 Create `repository/ContaRepository.kt` extending `JpaRepository<Conta, Int>`
- [x] 4.5 Create `repository/ContratacaoRepository.kt` extending `JpaRepository<Contratacao, Int>`
- [x] 4.6 Create `repository/TransacaoRepository.kt` extending `JpaRepository<Transacao, Long>`

## 5. Services

- [x] 5.1 Create `service/TipoTransacaoService.kt` with findAll, findById, create, update, delete; validate sinal in {-1, 1}
- [x] 5.2 Create `service/ClienteService.kt` with findAll, findById, create, update, delete
- [x] 5.3 Create `service/ProdutoService.kt` with findAll, findById, create, update, delete; validate taxa_juros ≥ 0
- [x] 5.4 Create `service/ContaService.kt` with findAll, findById, create, update, delete; resolve cliente FK
- [x] 5.5 Create `service/ContratacaoService.kt` with findAll, findById, create, update, delete; resolve cliente and produto FKs
- [x] 5.6 Create `service/TransacaoService.kt` with findAll, findById, create, update, delete; validate valor > 0; resolve all FKs; handle idempotency conflict as 409

## 6. REST Controllers

- [x] 6.1 Create `controller/TipoTransacaoController.kt` — `@RestController @RequestMapping("/api/tipos-transacao")` with POST, GET (list), GET (by id), PUT, DELETE
- [x] 6.2 Create `controller/ClienteController.kt` — `@RequestMapping("/api/clientes")` with full CRUD
- [x] 6.3 Create `controller/ProdutoController.kt` — `@RequestMapping("/api/produtos")` with full CRUD
- [x] 6.4 Create `controller/ContaController.kt` — `@RequestMapping("/api/contas")` with full CRUD
- [x] 6.5 Create `controller/ContratacaoController.kt` — `@RequestMapping("/api/contratacoes")` with full CRUD
- [x] 6.6 Create `controller/TransacaoController.kt` — `@RequestMapping("/api/transacoes")` with full CRUD
- [x] 6.7 Create `GlobalExceptionHandler.kt` — `@RestControllerAdvice` mapping `EntityNotFoundException` → 404, `DataIntegrityViolationException` → 409, `IllegalArgumentException` → 400

## 7. DTOs / Request-Response models

- [x] 7.1 Create request/response data classes for TipoTransacao (TipoTransacaoRequest, TipoTransacaoResponse)
- [x] 7.2 Create request/response data classes for Cliente
- [x] 7.3 Create request/response data classes for Produto
- [x] 7.4 Create request/response data classes for Conta
- [x] 7.5 Create request/response data classes for Contratacao
- [x] 7.6 Create request/response data classes for Transacao

## 8. Testcontainers Integration Tests

- [x] 8.1 Create `src/test/kotlin/.../TestcontainersConfiguration.kt` — `@TestConfiguration` with shared `MySQLContainer` bean using `@ServiceConnection`
- [x] 8.2 Create `TipoTransacaoControllerTest.kt` — `@SpringBootTest @AutoConfigureMockMvc` covering create, list, findById, update, delete scenarios
- [x] 8.3 Create `ClienteControllerTest.kt` — full CRUD integration test + duplicate cpf → 409
- [x] 8.4 Create `ProdutoControllerTest.kt` — full CRUD + negative taxa_juros → 400
- [x] 8.5 Create `ContaControllerTest.kt` — full CRUD + non-existent cliente → 404
- [x] 8.6 Create `ContratacaoControllerTest.kt` — full CRUD + duplicate constraint → 409
- [x] 8.7 Create `TransacaoControllerTest.kt` — full CRUD + valor ≤ 0 → 400 + duplicate idempotencia → 409
- [x] 8.8 Run `./gradlew :cap-theorem-mysql-cassandra:cap-theorem-mysql-cassandra:test` and verify all tests pass
