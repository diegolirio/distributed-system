## Context

The `system-kafka-migrate/system-async-kafka` service currently processes data migrations and asynchronous Kafka flows but does not expose a REST API for creating business orders. We need a new `POST /orders` endpoint that stores an order in MongoDB (initial status **PENDING**) and publishes an `order.created` event to Kafka so downstream services can react.

Constraints:
- The service is built with Java Spring Boot (assumed from existing codebase). 
- MongoDB and Kafka are external dependencies already configured for production. 
- All integration tests must run in CI without external services, therefore we adopt Testcontainers for both MongoDB and Kafka.
- The change should be production‑ready with proper error handling and observability.

Stakeholders: backend team, downstream event consumers, QA/CI pipelines.

## Goals / Non-Goals

**Goals:**
- Provide a reliable `POST /orders` API that validates input and persists an order with status `PENDING`.
- Emit a well‑defined Kafka event (`order.created`) after successful persistence.
- Ensure end‑to‑end correctness via Testcontainers‑based integration tests covering MongoDB and Kafka.
- Keep the change isolated to the `system‑kafka‑migrate/system‑async‑kafka` module.

**Non‑Goals:**
- Full order lifecycle management (updates, cancellations) – out of scope for this change.
- Implementing consumer logic for the `order.created` topic.
- Providing a UI or client SDK for order creation.

## Decisions

| Decision | Rationale |
|----------|-----------|
| **Use Spring Boot WebFlux (reactive) controller** | Aligns with existing asynchronous Kafka handling and enables non‑blocking IO for high throughput. |
| **Persist with MongoTemplate** (instead of a reactive repository) | Simpler for a single write operation and already used elsewhere in the module. |
| **Publish Kafka event after DB insert (synchronous flow)** | Guarantees that only persisted orders generate events; failures in publishing will surface as HTTP 502, prompting caller to retry. |
| **Outbox pattern not required** | The operation is simple; the risk of duplicate events is low and can be mitigated with idempotent consumer handling. |
| **Testcontainers for MongoDB (mongo:6.0) and Confluent Kafka (confluentinc/cp-kafka:7.5)** | Provides deterministic, isolated integration tests and matches the versions used in production. |
| **Schema for Kafka event: JSON** | Existing services consume JSON events; avoids needing Avro/Schema Registry for this small payload. |
| **Validation with Bean Validation (JSR‑380)** | Guarantees payload correctness before DB insert. |
| **CI pipeline update to run `mvn verify` with Testcontainers** | Guarantees tests run on every pull request without external service setup. |

## Risks / Trade‑offs

- **Risk:** Publishing to Kafka may fail after DB commit, leading to orphaned orders.
  - **Mitigation:** Return `502 Bad Gateway` to client; implement a retry endpoint or manual reprocess job later.
- **Risk:** Testcontainers increase CI execution time.
  - **Mitigation:** Use lightweight Docker images and parallelize tests where possible.
- **Risk:** Using a synchronous publish could block request threads under high load.
  - **Mitigation:** Keep the endpoint lightweight; monitor latency and consider async publishing in a future iteration.
- **Risk:** JSON schema may evolve, breaking consumers.
  - **Mitigation:** Version the Kafka topic (`order.created.v1`).

## Open Questions

- Should the endpoint return the created order representation (including generated ID) or just the ID?
- Do we need to add a correlation ID header to the Kafka message for tracing?
- Is there an existing audit/logging interceptor we should reuse for this controller?
