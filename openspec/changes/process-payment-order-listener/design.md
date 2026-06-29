## Context

The system successfully creates orders and publishes an `order.created` event to Kafka. The next step in the flow is to capture this event, issue an HTTP POST request to an external Payment Service to process the payment, and update the order state. The challenge is ensuring robust failure handling (using retries and DLQ) and avoiding double charges when network issues or component failures occur (using Idempotency-Key).

## Goals / Non-Goals

**Goals:**
- Reliable processing of `order.created` events.
- Idempotent interaction with the external Payment Service to prevent duplicate payments.
- Clear order status state management (`PROCESSED` or `FAILED`).

**Non-Goals:**
- Implementing the Payment Service itself (it is external, we will mock it).
- Handling the refund or saga pattern explicitly (since we assume the Payment Service is idempotent via the provided header).

## Decisions

- **Idempotency Strategy**: We will include an `Idempotency-Key: <orderId>` header in the HTTP request to the external Payment Service. This ensures that even if Kafka retries a message and we issue a duplicate POST, the Payment Service will recognize the key and safely return the same response without charging twice.
- **HTTP Client**: Use Spring `WebClient` since `spring-boot-starter-webflux` is already a dependency in the project. It provides non-blocking IO which is ideal for consuming messages and making external calls concurrently.
- **Error Handling**: Configure a `DefaultErrorHandler` for Spring Kafka with a `FixedBackOff` (e.g., 3 retries, 1 second apart). Exhausted retries will be sent to a Dead Letter Topic (DLT) via `DeadLetterPublishingRecoverer`.
- **Database Status**: The order status will only be updated to `PROCESSED` upon a successful HTTP response. If the HTTP call repeatedly fails and the message is routed to the DLQ, a DLQ listener (or the recoverer logic) can update the status to `FAILED`.

## Risks / Trade-offs

- **Risk**: The external Payment Service might not honor the `Idempotency-Key` correctly.
  - **Mitigation**: This is an external dependency risk. Integration tests with WireMock will verify that *we* are sending the header correctly.
- **Risk**: Database write fails *after* successful payment.
  - **Mitigation**: Kafka will retry. Because of the `Idempotency-Key`, the subsequent retry will safely call the Payment Service again (which will return a success without charging), and we can then successfully update the database.
