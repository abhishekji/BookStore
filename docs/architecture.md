# Architecture decisions

## Modular monolith and layered architecture

The first deployment is one Spring Boot application with explicit domain, application, API, repository, infrastructure, and security boundaries. This keeps local development and transactions simple while making future extraction possible at domain seams.

## Domain model

`UserAccount`, `Book`, `Cart`/`CartItem`, and `Order`/`OrderItem` are aggregate-oriented entities. Aggregates protect their own invariants; services coordinate use cases and repositories persist them.

## DTO/entity separation

Controllers return DTOs rather than JPA entities. This prevents persistence details and lazy-loading behavior from leaking into the API and gives the contract independent evolution.

## Database choice

H2 is the zero-configuration local database. MySQL is supported through a Spring profile and environment variables, with schema validation enabled for production-like deployments. A migration tool can be introduced before production data is managed.

## TDD approach

Domain tests are written before or alongside production behavior and do not require Spring. Application tests use Mockito for ports. API integration tests can be added as endpoints become business-complete.

## Scalability decisions

Keep synchronous transactions and a single deployable unit initially. Add database migrations, observability export, caching, an outbox, and asynchronous messaging only when measured requirements justify them. Authentication and checkout will be implemented behind application interfaces so payment and identity providers remain replaceable.
