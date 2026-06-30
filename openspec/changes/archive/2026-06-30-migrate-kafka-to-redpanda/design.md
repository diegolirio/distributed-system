## Context

The project is currently using Apache Kafka for messaging. To explore lowering operational overhead and resource usage, we are exploring a migration to RedPanda, a C++ Kafka-compatible broker that eliminates JVM and Zookeeper requirements. We want to validate this migration on the `system-async-kafka-migrate-redpanda` module without affecting the primary Kafka infrastructure.

## Goals / Non-Goals

**Goals:**
- Validate that RedPanda is a 100% drop-in replacement for our current Spring Kafka usage.
- Configure isolated infrastructure (via Testcontainers or Docker Compose) to test the `system-async-kafka-migrate-redpanda` module against RedPanda.

**Non-Goals:**
- Migrating the production Kafka cluster.
- Touching any other module or service that relies on Kafka.
- Changing the business logic of the messaging layer.

## Decisions

- **Infrastructure for Tests**: We will use `testcontainers-redpanda` (or a docker-compose setup) for integration tests to ensure that RedPanda spins up independently and cleanly tears down, guaranteeing zero impact on the existing Kafka environment.
- **Library Compatibility**: We will continue using `spring-boot-starter-kafka`. The only change will be the bootstrap-servers connection string pointing to the RedPanda instance.

## Risks / Trade-offs

- **Risk: Discrepancies between Kafka and RedPanda behaviors in edge cases.**
  - **Mitigation:** We will run all existing integration tests against the RedPanda container to ensure standard behaviors (e.g., offsets, retries, serialization) remain intact.
- **Risk: Storage cost differences on AWS (EBS vs S3).**
  - **Mitigation:** Out of scope for this code change, but documented in research.md for the infrastructure team.
