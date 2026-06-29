## 1. Setup

- [x] 1.1 Add `spring-cloud-contract-wiremock` (or `wiremock-standalone`) dependency to `build.gradle` for integration testing.
- [x] 1.2 Add external payment service base URL (`payment.service.url`) to `application.properties`.

## 2. Kafka Listener Configuration

- [x] 2.1 Configure a `DefaultErrorHandler` bean with a `FixedBackOff` (max 3 retries).
- [x] 2.2 Configure a `DeadLetterPublishingRecoverer` in the error handler to route persistently failed messages to `order.created.DLT`.

## 3. Payment Service Client

- [x] 3.1 Create a `PaymentServiceClient` using Spring `WebClient` pointing to the configured `payment.service.url`.
- [x] 3.2 Implement a `processPayment(String orderId)` method that sends a POST request to `/process-payment-order` including the `Idempotency-Key: <orderId>` header.

## 4. Listener Implementation

- [x] 4.1 Create an `OrderCreatedListener` class annotated with `@KafkaListener(topics = "order.created")`.
- [x] 4.2 In the listener, fetch the pending order by ID from `OrderRepository`. If already `PROCESSED`, silently acknowledge and skip.
- [x] 4.3 Invoke `PaymentServiceClient.processPayment(orderId)` within the listener.
- [x] 4.4 Upon a successful HTTP response, update the order status in the repository to `PROCESSED`.

## 5. DLQ Handling

- [x] 5.1 (Optional/Recommended) Create a DLQ listener for `order.created.DLT` that fetches the order and updates its status to `FAILED`.

## 6. Testing

- [ ] 6.1 Write an integration test using WireMock to mock a successful `200 OK` payment response and verify the order is updated to `PROCESSED`.
- [ ] 6.2 Write an integration test where WireMock returns `500 Internal Server Error` on the first attempt but `200 OK` on the retry, verifying recovery.
- [ ] 6.3 Write an integration test where WireMock persistently returns `500 Internal Server Error`, verifying the message is routed to the DLT and (if implemented) order status is `FAILED`.
