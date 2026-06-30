## Why

We are migrating our messaging infrastructure from Apache Kafka to RedPanda to take advantage of its C++ architecture, which removes the need for JVM and Zookeeper, drastically reducing operational complexity and CPU/RAM usage. We want to test this migration thoroughly in an isolated environment without affecting the existing Kafka infrastructure.

## What Changes

- Set up a new local RedPanda instance (using testcontainers or docker-compose) for testing purposes.
- Update `system-async-kafka-migrate-redpanda` module to connect to RedPanda instead of Kafka.
- Create tests specifically verifying compatibility and performance with RedPanda.
- **IMPORTANT**: The existing Kafka infrastructure must remain completely untouched.

## Capabilities

### New Capabilities
- `redpanda-integration`: Integrate the system with RedPanda, validating drop-in compatibility with Spring Kafka without changing application logic.

### Modified Capabilities
- (None)

## Impact

- The `system-async-kafka-migrate-redpanda` module and its configurations (`application.properties`, `application-test.properties`).
- Integration tests will spin up RedPanda containers instead of Kafka ones.
- No impact on production Kafka cluster or other applications.
