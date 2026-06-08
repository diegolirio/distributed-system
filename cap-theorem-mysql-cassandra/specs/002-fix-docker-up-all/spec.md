# Feature Specification: One-Command Full-Stack Startup (`docker-up-all`)

**Feature Branch**: `002-fix-docker-up-all`  
**Created**: 2026-06-07  
**Status**: Draft  
**Input**: User description: "fixes everything to run docker-up-all"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Bring the whole stack up with one command (Priority: P1)

A developer who just cloned the project runs a single command (`make docker-up-all`) and, with no manual steps in between, ends up with the application and its database running together, the database schema created, and the application reachable in a browser.

**Why this priority**: This is the entire point of the feature. Today the command chain is broken (the application image targets an older Java runtime than the one the application is built for, the application package is not (re)built before the image is assembled, and the package-copy step is ambiguous about which artifact to ship). Until the one-command path works end to end, nothing else about it matters.

**Independent Test**: On a clean machine with only the container tooling installed, run the single command and confirm the application answers on its web port and the database is populated with the expected schema — without editing any file or running any other command first.

**Acceptance Scenarios**:

1. **Given** a clean checkout and a machine with only the container runtime available, **When** the developer runs the single startup command, **Then** the database service and the application service both start and reach a healthy/running state.
2. **Given** the stack has started, **When** the developer opens the application's web address, **Then** the application responds successfully (home page / health endpoint) rather than failing to start.
3. **Given** the application has started, **When** the database is inspected, **Then** all expected tables exist (schema migrations have been applied).
4. **Given** source code has changed since the last run, **When** the developer runs the single startup command again, **Then** the running application reflects the latest build (the artifact and image are rebuilt, not stale).

---

### User Story 2 - The application starts only after the database is ready (Priority: P2)

When the stack comes up, the application waits for the database to be ready before it tries to connect, so a cold start does not fail intermittently.

**Why this priority**: Ordering/timing failures are the most common cause of a "sometimes it works" first-run experience. It builds directly on Story 1 and makes the one-command path reliable rather than flaky.

**Independent Test**: Start the stack from a completely cold state (no pre-existing database) several times and confirm the application reaches a running state every time without a manual restart.

**Acceptance Scenarios**:

1. **Given** no database container is running yet, **When** the stack is started cold, **Then** the application does not attempt to connect until the database reports healthy, and it ultimately starts successfully.
2. **Given** a cold start is performed repeatedly, **When** measured across several attempts, **Then** the application reaches a running state on every attempt without manual intervention.

---

### User Story 3 - Predictable teardown and restart (Priority: P3)

A developer can stop the stack and start it again cleanly, getting back a working environment, and can choose whether previously stored data is retained.

**Why this priority**: Day-to-day usability after the first success. Lower priority because the first-run success (Stories 1–2) is the blocking problem.

**Independent Test**: Bring the stack up, stop it with the documented command, bring it up again, and confirm the application is reachable and behaves consistently.

**Acceptance Scenarios**:

1. **Given** the stack is running, **When** the developer runs the documented stop command, **Then** all of the stack's services are stopped.
2. **Given** the stack was stopped, **When** the developer starts it again, **Then** the application becomes reachable again without manual fixes.

---

### Edge Cases

- What happens when the configured web port or database port is already in use on the host? The startup fails fast with a clear indication of the conflicting port rather than hanging.
- What happens when the application artifact has not been produced yet (first ever run)? The startup command produces it as part of the flow rather than failing on a missing artifact.
- What happens when more than one application artifact exists (e.g., a runnable package and a secondary library package)? The image ships the correct runnable one, unambiguously.
- What happens when the database already contains data from a previous run? The application starts against the existing data without re-failing on already-applied schema changes.
- What happens when the application runtime version is older than the version the application was built for? This must not occur — the runtime used to run the application must be compatible with the version it was built with.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A single documented command MUST bring up the complete stack (application + database) from a clean checkout with no manual intermediate steps.
- **FR-002**: The single command MUST (re)produce the application artifact before assembling the application image, so the running application always reflects the current source.
- **FR-003**: The application image MUST run on a runtime version compatible with the version the application was built for (no runtime-older-than-build mismatch).
- **FR-004**: The image assembly MUST ship exactly one, unambiguous runnable application artifact, even when the build produces more than one artifact.
- **FR-005**: The application service MUST wait until the database service reports healthy before attempting to connect.
- **FR-006**: On startup, the database schema MUST be created/updated automatically (all expected tables present) without manual scripts.
- **FR-007**: The application MUST be reachable on its documented web port after startup completes.
- **FR-008**: The application and database services MUST share configuration (addresses, credentials, database name) consistently so the application can connect using the in-stack database rather than a host-local one.
- **FR-009**: A single documented command MUST stop the entire stack, and the same up-command MUST be repeatable to restart it.
- **FR-010**: Re-running the up-command after a source change MUST result in the updated application running (no stale artifact or image).
- **FR-011**: The startup flow MUST fail fast with an understandable message when a required host port is already in use.
- **FR-012**: The expected commands and any prerequisites MUST be documented so a new developer can run the stack without prior knowledge of the internals.

### Key Entities *(include if feature involves data)*

- **Application service**: The runnable application packaged as a container image; needs a compatible runtime and the correct runnable artifact; exposes a web port; connects to the database service.
- **Database service**: The data store packaged as a container; exposes a database port; reports a health state; holds persisted data across restarts when retention is chosen.
- **Stack definition**: The declarative description that ties the two services together (shared network, configuration, startup ordering, health dependency).
- **Startup command set**: The documented commands to build, start, and stop the stack as a unit.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new developer can go from a clean checkout to a reachable application using a single command, with zero manual edits or extra commands.
- **SC-002**: Cold starts succeed on 100% of attempts across at least 5 consecutive cold runs, with no manual restart needed.
- **SC-003**: After startup, 100% of the expected database tables are present.
- **SC-004**: The running application reflects the latest source on every re-run (0 stale-artifact incidents across repeated runs after code changes).
- **SC-005**: Time from running the single command to the application being reachable is under 5 minutes on a typical developer machine (excluding first-time base-image downloads).
- **SC-006**: A developer unfamiliar with the project can start and stop the stack using only the written documentation, without asking for help.

## Assumptions

- "Everything" in the request refers to the existing `docker-up-all` flow for the `cap-theorem-mysql-cassandra` application and its MySQL database; no new services (e.g., the Cassandra side) are added by this feature.
- The container runtime and the compose/orchestration tooling are already installed on the developer's machine; installing them is out of scope.
- The application continues to use its existing relational database and automatic schema-migration mechanism; this feature fixes the startup/packaging path, not the data model.
- Default credentials, database name, and ports already defined for the demo environment are acceptable and are reused; secret management/hardening is out of scope for this educational project.
- The fix targets local/developer use; production deployment, scaling, and orchestration platforms are out of scope.
- Data retention across restarts uses the existing persistent volume; choosing to wipe data is a deliberate, separate action.
- Network access to download base images is available on first run.
