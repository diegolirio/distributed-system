## Why

The current system lacks an API to create orders in a reliable, test‑driven manner. Introducing an `POST /orders` endpoint that stores an order in MongoDB with an initial status of **PENDING** and publishes a creation event to Kafka will enable downstream services to react to new orders and improve overall data flow. Implementing this feature with Testcontainers‑based TDD ensures that the integration with MongoDB and Kafka is exercised in automated tests, increasing confidence and reducing regression risk.

## What Changes

- Add a new HTTP endpoint `POST /orders` in the `system-kafka-migrate/system-async-kafka` service.
- Define the order payload (`productName`, `productPrice`, `productAmount`, `status`).
- Persist the order in MongoDB with `status: PENDING`.
- Publish an `order.created` event to a Kafka topic after successful persistence.
- Introduce a comprehensive Testcontainers test suite covering MongoDB and Kafka interactions.
- Update CI pipelines to run the new integration tests.

## Capabilities

### New Capabilities
- `order-creation`: Enables creation of orders via a REST API, persisting them to MongoDB and emitting a creation event on Kafka.

### Modified Capabilities
- *(none)*

## Impact

- **Codebase**: New controller/handler, service layer, repository for orders, and Kafka producer integration.
- **Dependencies**: Adds Testcontainers dependencies for MongoDB and Kafka to the test scope.
- **APIs**: Introduces `/orders` endpoint; may require updates to API documentation (OpenAPI/Swagger).
- **Systems**: Downstream consumers of the `order.created` topic will now receive events for newly created orders.
