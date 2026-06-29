## Why

After an order is successfully created and the `order.created` event is published, the system needs to process the payment for that pending order. This proposal introduces a Kafka listener that consumes the event, calls an external payment service, and updates the order status, completing the order creation lifecycle securely and idempotently.

## What Changes

- Add a Kafka listener to consume `order.created` events.
- Implement an HTTP client to send a POST request to the external Payment Service (`payment-service-host/process-payment-order`).
- Configure the HTTP client to include an `Idempotency-Key` header (using the unique `orderId`) to prevent double charging on retries.
- Update the order status in MongoDB to `PROCESSED` upon successful payment, or `FAILED` after exhausting retries.
- Setup retry logic (max 3 retries) and route persistently failing messages to a Dead Letter Queue (DLQ).
- Use WireMock in integration tests to mock the external Payment Service.

## Capabilities

### New Capabilities
- `process-payment-order`: Defines the requirements for listening to order creation events, calling the payment service idempotently, updating order status, and handling failures via retries and DLQ.

### Modified Capabilities
None.

## Impact

- **Affected Code**: `system-async-kafka` service.
- **Dependencies**: New dependency on `spring-cloud-contract-wiremock` (or standalone WireMock) for test scope.
- **Systems**: External HTTP communication with the Payment Service. Local MongoDB updates.
