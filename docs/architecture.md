# Architecture decisions

## Modular monolith and layered architecture

The first deployment is one Spring Boot application with explicit domain, application, API, repository, infrastructure, and security boundaries. This keeps local development and transactions simple while making future extraction possible at domain seams.

## Domain model

`UserAccount`, `Book`, `Cart`/`CartItem`, and `Order`/`OrderItem` are aggregate-oriented entities. Aggregates protect their own invariants; services coordinate use cases and repositories persist them.

## DTO/entity separation

Controllers return DTOs rather than JPA entities. This prevents persistence details and lazy-loading behavior from leaking into the API and gives the contract independent evolution.

## Database choice

H2 is the zero-configuration local database. MySQL is supported through a Spring profile and environment variables, with schema validation enabled for production-like deployments. A migration tool can be introduced before production data is managed.

The current tables, keys, generated collection join tables, database security model,
replica/sharding guidance, and PostgreSQL migration notes are documented in
`docs/database-schema.md`.

## TDD approach

Domain tests are written before or alongside production behavior and do not require Spring. Application tests use Mockito for ports. API integration tests can be added as endpoints become business-complete.

## Scalability decisions

Keep synchronous transactions and a single deployable unit initially. Add database migrations, observability export, caching, an outbox, and asynchronous messaging only when measured requirements justify them. Authentication and checkout will be implemented behind application interfaces so payment and identity providers remain replaceable.

## Design patterns and long-term evolution

The codebase uses patterns where they solve an existing boundary:

- **Repository**: Spring Data repositories isolate persistence from application use cases.
- **Service layer**: application services coordinate use cases without placing orchestration in controllers.
- **Domain model / aggregate**: `Book`, `Cart`, and `Order` protect their own invariants.
- **DTO / data mapper**: API response records prevent persistence entities from becoming API contracts.
- **Ports and adapters**: replaceable integrations such as identity, payment, inventory, and event publishing should be represented by application interfaces with infrastructure adapters.
- **Saga orchestration**: `SagaOrchestrator` executes compensatable workflow steps in order and compensates completed steps in reverse order. `CheckoutContext` and `CheckoutStep` provide the checkout seam for future payment, inventory, order, and cart steps.
- **Builder**: `Order.Builder` keeps order construction readable as checkout adds timestamps, line items, totals, and metadata.
- **Strategy**: `PaymentStrategy` isolates payment authorization and refund behavior for different payment methods or providers.
- **Factory**: `PaymentStrategyFactory` chooses a payment strategy from the configured implementations and keeps application services independent of provider classes.
- **Idempotency record**: `CheckoutIdempotencyRecord` and its unique user/key constraint make checkout retries safe across threads and application instances.

These patterns are deliberately implemented at existing variation points. A future payment provider adapter can implement `PaymentStrategy`; checkout does not need to change when another provider is added.

The current deployment is a modular monolith. Checkout should initially use one database transaction as the consistency boundary; the saga seam is for compensating external side effects such as payment authorization or notification. A saga must not replace atomic database locking for stock or idempotency. If the system later separates modules into services, the same step contracts can be adapted to messaging and an outbox without moving business rules into controllers.
