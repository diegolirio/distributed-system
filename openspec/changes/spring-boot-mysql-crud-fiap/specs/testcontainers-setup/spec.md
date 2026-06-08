## ADDED Requirements

### Requirement: Shared Testcontainers MySQL configuration for integration tests
The module SHALL provide a shared `MySQLContainer` instance (via Kotlin `companion object`) that is reused across all test methods in a class to avoid redundant container startup.

#### Scenario: Container starts once per test class
- **WHEN** a test class annotated with `@Testcontainers` runs multiple test methods
- **THEN** the MySQL container starts exactly once and is reused for all methods in that class

### Requirement: Spring datasource is auto-configured from the container
The test context SHALL use `@DynamicPropertySource` or a Testcontainers `@ServiceConnection` to override `spring.datasource.url`, `username`, and `password` with values from the running container.

#### Scenario: Application context connects to the Testcontainers MySQL
- **WHEN** a `@SpringBootTest` test runs with the Testcontainers MySQL container active
- **THEN** the Spring datasource connects to the container URL and Flyway migrations execute successfully

### Requirement: Testcontainers dependencies are correctly declared
`build.gradle.kts` SHALL include:
- `testImplementation("org.springframework.boot:spring-boot-starter-testcontainers")`
- `testImplementation("org.testcontainers:testcontainers-junit-jupiter")`
- `testImplementation("org.testcontainers:mysql")`
- `testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")`

#### Scenario: Build compiles and tests run without missing class errors
- **WHEN** `./gradlew test` is executed
- **THEN** the build resolves all Testcontainers classes and tests run without `ClassNotFoundException`
