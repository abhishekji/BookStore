# Engineering guidelines

This document records the engineering standards for the bookstore modular monolith.

## Object-oriented and domain design

The backend uses a rich domain model rather than treating entities as database-shaped records. Aggregates own their invariants and state transitions:

- `Cart` owns adding, combining, and removing `CartItem` values.
- `Order` owns order-item composition and order status.
- `Book` owns price, stock, and availability invariants.

Application services coordinate use cases and transactions. Controllers translate HTTP requests and responses. DTOs define API contracts and do not contain business decisions.

Use interfaces at real replaceable boundaries such as identity, clock, inventory, and
event publishing. The current checkout seam demonstrates the following pattern:

- **Builder**: `Order.Builder` creates an order while keeping optional creation-time and item composition details out of constructors.

Keep each pattern focused on a real variation point. Do not add factories, strategies, or builders merely to increase abstraction; a single stable algorithm should remain simple.

## Transactional workflows

The modular monolith uses a database transaction as the consistency boundary for
checkout. Order creation, stock reservation, idempotency persistence, and cart
mutation remain in the same transaction; controllers must not coordinate those
operations directly.

External payment, message, or fulfilment integrations should be introduced only when
there is a concrete requirement. Model each integration behind an application port and
add an outbox before publishing domain events to an external broker.

## Transactional integrity

Critical operations must be atomic:

- Cart mutations must not leave an invalid cart state.
- Checkout must create the order, reserve/decrement stock, and clear the cart as one transaction.
- A failed stock reservation must roll back the order and cart changes.
- Stock reservation must use an atomic conditional update or a locking/version strategy so concurrent requests cannot oversell inventory.

## Checkout idempotency

Checkout requests require a client-provided idempotency key. The backend should persist a record containing at least:

- authenticated user identifier;
- idempotency key;
- request fingerprint or relevant request version;
- resulting order identifier and response status;
- creation and expiry timestamps.

The combination of user and idempotency key must be unique. Repeating the same request returns the original result without creating another order or deducting stock. Reusing a key for a different request must return a validation/conflict error.

The idempotency record and checkout result must be committed atomically with the order and stock operation. A retry after a transient failure must not create a duplicate order.

The current implementation uses `CheckoutIdempotencyRecord` and `CheckoutIdempotencyService`. `begin` claims a user-scoped key, compares the request fingerprint, returns a previously completed order for an identical retry, and rejects concurrent processing. A database unique constraint on `(user_id, idempotency_key)` remains the final protection across multiple application instances. The checkout use case must call `begin`, perform order/stock/cart work in the same transaction, and call `complete` only after the order is durably created. A failed transaction must roll back the claim and stock/order changes together.

## Validation and errors

Boundary validation uses Bean Validation where appropriate. Domain constructors and methods enforce the same invariants independently of HTTP. Centralized exception handling returns a stable error shape and correlation ID without leaking internal details.

## Structured logging

Business events should be logged with structured fields:

```text
event=checkout_completed correlationId=... userId=... orderId=... outcome=success
event=stock_reservation_rejected correlationId=... bookId=... requestedQuantity=... outcome=insufficient_stock
```

Sensitive credentials, tokens, payment information, and raw authorization headers must never be logged.

Application services must use the `BusinessEventLogger` port for business events rather than constructing logger calls throughout use cases. The current adapter emits events for cart add/remove and checkout start/completion/replay/rejection. Correlation IDs are added by `CorrelationIdFilter` through the logging context, so business logs can be correlated without passing request metadata through domain objects. Log identifiers and quantities only; never log request fingerprints, passwords, payment details, or authorization headers.

## Secrets management

Secrets must be externalized. Do not commit database passwords, signing keys, access tokens, API keys, or production usernames. Use Spring Cloud Vault Config for production secret delivery and environment variables only as a controlled local/CI fallback. Production configuration must fail closed when required secrets are missing and must not define insecure credential defaults.

The default H2 profile is intentionally local-only and contains no production secret. Any future JWT signing key, OAuth client secret, payment credential, or checkout idempotency encryption key must be loaded from Vault and represented in configuration properties rather than scattered through application code.

## TDD expectations

New behavior starts with tests. Required scenarios should include:

- valid and invalid domain construction;
- boundary quantities and prices;
- repository query behavior;
- application-service happy and failure paths;
- HTTP status and error contracts;
- frontend loading, empty, success, unavailable, and error states;
- concurrent stock reservation;
- duplicate checkout and idempotency conflicts;
- transaction rollback.

Use fixtures/builders for repeated data. Keep production configuration and test data out of business logic.
