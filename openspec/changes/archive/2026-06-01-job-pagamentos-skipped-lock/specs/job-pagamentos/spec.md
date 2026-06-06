## ADDED Requirements

### Requirement: Job polls requests table in a continuous loop
The system SHALL execute a `@Scheduled` job that loops over the `requests` table, fetching and processing rows until none remain with status `PENDING`.

#### Scenario: Loop runs until no pending rows
- **WHEN** the job starts and pending rows exist
- **THEN** the job fetches batches of up to 100 rows and processes each, repeating until a batch returns empty

#### Scenario: Loop terminates when no rows found
- **WHEN** a batch query returns zero rows
- **THEN** the job exits the loop and completes without error

### Requirement: Job uses configurable batch size
The system SHALL read the batch size from `app.job.batch-size` (default `100`) so operators can tune throughput without code changes.

#### Scenario: Batch size respected
- **WHEN** `app.job.batch-size=50` is set and 200 pending rows exist
- **THEN** each loop iteration processes exactly 50 rows

### Requirement: Job can be disabled via configuration
The system SHALL skip execution when `app.job.enabled=false` is set, allowing operators to disable the job without redeployment.

#### Scenario: Job disabled
- **WHEN** `app.job.enabled=false`
- **THEN** the `@Scheduled` method returns immediately without querying the database
