## 1. Test Environment Setup

- [x] 1.1 Configure `testcontainers-redpanda` dependency in `build.gradle` for the `system-async-kafka-migrate-redpanda` module (replace `testcontainers-kafka` if necessary).
- [x] 1.2 Update the test bootstrap configuration to spin up a RedPanda container on integration tests instead of standard Kafka container.

### Phase 2: Testing & Validation
- [x] 2.1 Run all tests in the `system-async-kafka-migrate-redpanda` module to verify that connection, publishing, and consuming work correctly with RedPanda.
- [x] 2.2 Verify that the application starts cleanly with `application-test.properties` pointing to the ephemeral RedPanda cluster.
