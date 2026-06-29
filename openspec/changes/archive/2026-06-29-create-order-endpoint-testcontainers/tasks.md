## 1. Setup

- [x] 1.1 Add Maven/Gradle dependencies for Spring WebFlux, Spring Data MongoDB, and Spring Kafka if not already present.
- [x] 1.2 Add Testcontainers dependencies for MongoDB (mongo:6.0) and Confluent Kafka (confluentinc/cp-kafka:7.5) in the test scope.
- [x] 1.3 Configure application.yml (or application.properties) for MongoDB connection and Kafka bootstrap servers for the test profile.

## 2. Domain Model & Validation

- [x] 2.1 Create `OrderDto` class with fields `productName`, `productPrice`, `productAmount` and validation annotations (e.g., @NotBlank, @Positive).
- [x] 2.2 Create `Order` entity class that maps to the MongoDB collection, includes `status` field defaulting to `PENDING` and generated `orderId`.

## 3. Persistence Layer

- [x] 3.1 Implement `OrderRepository` using `MongoTemplate` (or Spring Data) for inserting orders.
- [x] 3.2 Write unit tests for repository save operation using Testcontainers MongoDB.

## 4. Kafka Integration

- [x] 4.1 Create a Kafka producer bean that serializes `Order` to JSON and publishes to the `order.created` topic.
- [x] 4.2 Write a unit test for the producer using Testcontainers Kafka, verifying the message is sent.

## 5. REST Endpoint

- [x] 5.1 Implement `OrderController` with `@PostMapping("/orders")` that accepts `OrderDto`, validates it, converts to `Order`, saves via repository, then publishes the Kafka event.
- [x] 5.2 Add error handling for validation failures (400) and Kafka publish failures (502).
- [x] 5.3 Write integration test (SpringBootTest) that starts both MongoDB and Kafka Testcontainers, sends a POST request, and asserts:
    - Order is persisted with `status: PENDING`.
    - An `order.created` event appears on Kafka with correct payload.

## 6. Documentation & CI

- [x] 6.1 Update OpenAPI/Swagger definition to include the new `/orders` endpoint.
- [x] 6.2 Add CI pipeline step to run `mvn verify` (or equivalent) ensuring Testcontainers tests execute.
- [x] 6.3 Update README or developer docs with instructions on running tests locally (Docker required).

## 7. Cleanup & Review

- [x] 7.1 Perform code style/lint checks and address any issues.
- [x] 7.2 Conduct a peer code review and incorporate feedback.
- [x] 7.3 Merge the change into the main branch and tag the release.
