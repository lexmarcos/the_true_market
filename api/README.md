# The True Market API

This project is a production-grade Spring Boot service that analyzes Counter-Strike 2 (CS2) skin market data. It ingests bot-produced events through RabbitMQ, persists normalized information to PostgreSQL, and exposes REST endpoints that help trading agents decide whether a skin is worth buying based on price history, volatility, and profit expectations.

The documentation in this repository is written for large language models (LLMs). Each section below aims to be explicit about responsibilities, dependencies, inputs, and outputs so an autonomous agent can understand how to interact with the API or extend it safely.

---

## High-Level Goals

- Aggregate market listings from external marketplaces.
- Maintain canonical Steam price history for each skin and wear level.
- Compute profitability insights (discount, expected gain, net profit) using configurable business rules.
- Provide a clean interface for automation bots that request work, submit analysis results, and read profitable opportunities.

---

## Technology Stack

- **Language:** Java 17 (LTS) via Gradle toolchains.
- **Framework:** Spring Boot 3.5.7 with dependency management.
- **Messaging:** RabbitMQ for work distribution and event ingestion.
- **Database:** PostgreSQL 15 for persistent storage.
- **Documentation:** Spring REST Docs (tests generate AsciiDoc snippets).
- **Testing:** Spring Boot Test, RabbitMQ test containers, REST Docs.
- **Developer Tooling:** Lombok for low-friction POJO generation.

See `build.gradle` for the authoritative dependency list.

---

## Architecture Overview

The project enforces Clean Architecture and SOLID principles. Layer boundaries are visible in the package structure under `src/main/java/com/thetruemarket/api`:

- `domain/`: Pure domain models, repositories (interfaces), services, and value objects.
- `application/`: Use cases and DTOs orchestrating domain logic.
- `infrastructure/`: Framework adapters (JPA entities, controllers, messaging consumers, config, services).
- `TheTrueMarketApiApplication`: Spring Boot bootstrap class linking all layers via dependency injection.

Dependencies only point inwards (outer layers depend on inner layers). Domain remains framework-agnostic, making it simple to test business rules or port to another delivery mechanism.

---

## Core Domain Concepts

- **Skin:** Represents a CS2 skin listing with market metadata (price, currency, marketplace, wear level). Persisted via JPA adapters.
- **Wear (`domain.valueobject.Wear`):** Enum capturing float ranges for Factory New, Minimal Wear, Field-Tested, Well-Worn, and Battle-Scarred.
- **HistoryUpdateTask:** Work item signaling that a bot must fetch Steam price history for a specific skin and wear. Tasks transition from `WAITING` to `COMPLETED`.
- **SteamPriceHistory:** Aggregate storing the most recent Steam average price per skin and wear combination.
- **ProfitResult:** Value object produced by `ProfitCalculationService`, combining discount, net profit, and expected gain calculations.

---

## Application Use Cases

- **`GetPendingTasksUseCase`:** Returns FIFO-ordered `WAITING` history update tasks.
- **`CompleteHistoryUpdateTaskUseCase`:** Validates and closes tasks, converts average price from BRL cents to USD cents, saves new Steam history, and marks the task as `COMPLETED`.
- **`GetProfitableSkinsUseCase`:** Aggregates all skins with available pricing, joins with Steam history, and calculates discount/profit metrics. Supports filtering, sorting, and limiting.

Use cases depend exclusively on domain repositories and services, making them easy to mock in tests or reuse through other delivery mechanisms (messaging, CLI, etc.).

---

## REST Interface

Controllers live under `infrastructure/web/controller`. Each endpoint already has a dedicated machine-readable guide in the `docs/` directory.

1. **`GET /api/v1/history-update-tasks`** — Retrieve pending tasks (see `docs/history-update-tasks-get.md`).
2. **`POST /api/v1/history-update-tasks/{taskId}/complete`** — Submit Steam price history for a task (see `docs/history-update-task-complete.md`).
3. **`GET /api/v1/skins/profitable`** — Fetch profit analyses with optional filters and sorting (see `docs/skins-profitable-get.md`).

Each controller returns DTOs located in `infrastructure/web/dto`. Request/response shapes are mirrored in the doc files for easy LLM consumption.

---

## Messaging Workflow (RabbitMQ)

- Bots publish market discovery events to RabbitMQ queues (consumer implementations in `infrastructure/messaging`).
- `HistoryUpdateTask` records are created when new data requires Steam price verification.
- Clients poll the REST endpoint to fetch work and submit results once complete.
- Failed message processing should be routed to dead-letter queues (planned additions live under `infrastructure/messaging/config`).

RabbitMQ defaults are configured in `src/main/resources/application.properties` with exponential backoff, manual retry limits, and prefetched delivery of one task at a time.

---

## Persistence Layer (PostgreSQL)

- JPA entities reside under `infrastructure/persistence/entity` synced with domain models via mappers in `infrastructure/persistence/mapper`.
- Repositories in `infrastructure/persistence/repository` implement domain interfaces located in `domain/repository`.
- Default schema migrations rely on JPA auto DDL (`spring.jpa.hibernate.ddl-auto=update`). For production, replace with managed migrations (Flyway/Liquibase).

Database connection details (URL, username, password) are configurable via environment variables or the defaults declared in `application.properties`.

---

## Configuration & Bootstrapping

- `src/main/resources/application.properties` holds RabbitMQ, PostgreSQL, and logging defaults.
- `history.update.expiration-seconds` determines how long a task stays valid; adjust as needed for queue backlogs.
- Logging is set to `DEBUG` for project packages and AMQP, using a simplified console pattern.

The Spring context auto-wires beans via `@RequiredArgsConstructor` and Lombok to keep configuration minimal.

---

## Local Development Setup

1. **Start infrastructure dependencies:**
   ```bash
   docker compose up -d postgres rabbitmq
   ```
2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```
3. The API listens on `http://localhost:8080`. Adjust `application.properties` or environment variables to point at remote brokers/databases as needed.

`docker-compose.yml` defines ready-to-use services for PostgreSQL and RabbitMQ with health checks and persisted volumes.

---

## Testing & Documentation

- Execute the test suite with `./gradlew test`. This produces REST Docs snippets under `build/generated-snippets`.
- Integrate AsciiDoc generation by running `./gradlew asciidoctor` if you wish to publish HTML API docs.
- Consider adding integration tests in `src/test/java` for new endpoints or use cases. The project currently includes the basic Spring Boot test harness (`TheTrueMarketApiApplicationTests`).

---

## Best Practices for Contributors

- Maintain Clean Architecture boundaries: domain must not depend on Spring or infrastructure classes.
- Prefer constructor injection (already enforced through Lombok and `@RequiredArgsConstructor`).
- Keep domain entities immutable wherever practical; side effects should live in use cases or services.
- Add unit/integration tests alongside new behaviors, especially for financial calculations.
- Update `docs/` endpoints and this README when public contracts change.

---

## Extending the Platform

When adding new functionality, follow these steps to keep the architecture consistent:

1. **Model the domain**: Introduce new value objects or entities in `domain` without referencing Spring.
2. **Define repository/service ports**: Add interfaces under `domain/repository` or `domain/service`.
3. **Implement use cases**: Place orchestration logic in `application/usecase` returning DTOs under `application/dto`.
4. **Create adapters**: Implement persistence, messaging, or HTTP adapters inside `infrastructure` packages.
5. **Document endpoints**: Extend the `docs/` LLM-oriented files for any new routes.

---

## Additional Resources

- Docker helper: `README-DOCKER.md` covers container-based workflows.
- Postman artifacts: `postman_collection.json` and `postman_environment.json` provide example requests.
- Agent guidance: `AGENTS.md` and `CLAUDE.md` contain agent-specific onboarding information.

For clarifications or design discussions, align with the Clean Architecture prefixes described above before introducing large changes.
