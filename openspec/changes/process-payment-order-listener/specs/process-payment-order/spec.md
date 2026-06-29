## ADDED Requirements

### Requirement: Process Payment Order Event
The system SHALL consume the `order.created` event, fetch the pending order, and make an HTTP POST request to the external Payment Service (`/process-payment-order`). The request MUST include an `Idempotency-Key` header containing the `orderId`.

#### Scenario: Successful Payment Call
- **WHEN** the `order.created` event is received and the HTTP POST to the external service returns a success (2xx) response
- **THEN** the system SHALL update the order status in the database to `PROCESSED` and acknowledge the Kafka message

### Requirement: Error Handling and Retries
The system SHALL retry the external payment API call up to 3 times in case of failures (e.g., 5xx errors or network timeouts). If all retries are exhausted, the message MUST be sent to a Dead Letter Queue (DLQ).

#### Scenario: Temporary Failure Recovered
- **WHEN** the HTTP POST fails initially but succeeds on a subsequent retry
- **THEN** the system SHALL update the order status to `PROCESSED` and acknowledge the Kafka message

#### Scenario: Persistent Failure Exhausts Retries
- **WHEN** the HTTP POST fails across all configured retry attempts
- **THEN** the system SHALL route the message to the DLQ and the order status SHALL be updated to `FAILED`
