# Simple Online Bookstore

This project is a code kata for building a simple online bookstore with a React front end and a Spring Boot RESTful back end.

The target user journey is:

1. Display a catalogue of books.
2. Add books to a shopping cart.
3. Display the cart contents.
4. Increase or decrease item quantities.
5. Remove books from the cart.
6. Checkout safely and review the order confirmation.

## Checkout and orders

Authenticated users checkout with `POST /api/orders/checkout` and an `Idempotency-Key` header. The response contains the confirmed order, including price/title snapshots. `GET /api/orders` lists only the current user's orders and `GET /api/orders/{id}` returns an order only to its owner.

Checkout is atomic: the idempotency claim, cart lock, stock reservation, order/item persistence, cart clear, and claim completion are one transaction. Failures roll back all writes. `checkout_idempotency` has a database uniqueness constraint on `(user_id, idempotency_key)`, which works across backend instances. The same completed key replays the original order; an in-flight concurrent request gets `409`. The cart and requested books are pessimistically locked, with books locked in UUID order to avoid overselling and reduce deadlock risk.

Important persistence constraints/indexes are the cart's unique user key, inventory's unique book key, order user lookup, and idempotency's unique user/key lookup. Production MySQL deployments should add these schema changes through the team's migration tool before enabling `ddl-auto=validate`.

The React cart now leads to a checkout review and confirmation page. Each mounted checkout attempt creates one browser idempotency key and reuses it for retries; a new key is only created by abandoning and starting a new attempt.

Tests include domain snapshot/transition tests, checkout orchestration tests, idempotency-service unit tests, repository integration tests, and React component/client tests. Run `mvn -pl backend test`, then `npm.cmd test` and `npm.cmd run build` from `frontend`.

## Observability

The backend exposes health/readiness/liveness and Prometheus metrics through Spring Actuator:

| Purpose | URL | Notes |
|---|---|---|
| Health | `http://localhost:8080/actuator/health` | Includes liveness/readiness probes |
| Metrics scrape | `http://localhost:8080/actuator/prometheus` | Prometheus text format |
| Prometheus | `http://localhost:9090` | Query `http_server_requests_seconds_count` |
| Grafana | `http://localhost:3000` | Default local login is `admin` / `admin`; change it immediately |
| Loki | `http://localhost:3100` | Query logs from Grafana Explore |

Start the local stack from the repository root:

```bash
docker compose -f observability/docker-compose.yml up
```

Then start the backend with the `observability` profile, which writes structured application logs to `backend/logs/bookstore.log` for Promtail:

```bash
mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=observability
```

Prometheus scrapes the backend every 15 seconds. In Grafana, select the provisioned Prometheus data source for metrics and the Loki source for logs; correlate checkout logs with the `correlationId` field. The compose stack is local-development tooling, not a production deployment. In production, protect the metrics endpoint at the network/ingress layer and ship logs to centrally managed storage.

The frontend initializes Sentry only when `VITE_SENTRY_DSN` is configured. It intentionally sends no default PII and samples 10% of traces. Configure `VITE_RELEASE` from the deployment pipeline. No monitoring credential is committed to this repository.

For traffic safety, checkout retains database idempotency, cart and inventory write locks, deterministic book-lock order, and database connection-pool limits under the MySQL profile. These protect correctness; horizontal scale should still be validated with production-like load tests and MySQL lock-timeout monitoring.

## From scratch: local setup

1. Install JDK 17+, Maven 3.9+, Node.js 20+, npm, and optionally Docker Desktop for local observability.
2. Copy `.env.example` to `.env` in the repository root and set a random `BOOKSTORE_JWT_SECRET` of at least 32 characters. The backend imports this file at startup, so no shell export is needed. `.env` is gitignored; never commit a real secret. Exporting `BOOKSTORE_JWT_SECRET` in the environment also works and takes precedence, which is how deployed environments supply it. If neither is present the application fails to start rather than falling back to an insecure default.
3. Start the API: `mvn -pl backend spring-boot:run`. It uses H2 by default at `http://localhost:8080`.
4. In a second terminal run `cd frontend`, `npm ci`, then `npm run dev`. Open `http://localhost:5173`. The dev server is pinned to port 5173 because that origin is the one the backend's CORS allow-list trusts; if the port is already taken, Vite stops with an error instead of moving to another port and leaving every API call blocked by CORS. Free the port, or add the new origin to `bookstore.cors.allowed-origins`.
5. Browse the OpenAPI UI at `http://localhost:8080/swagger-ui/index.html`; raw OpenAPI is `http://localhost:8080/v3/api-docs`.

### Testing and coverage

Backend tests are package-mirrored JUnit/Mockito unit and JPA integration tests. Run `mvn -pl backend verify` to create `backend/target/site/jacoco/index.html`. Frontend tests are colocated with features and use Testing Library/Vitest. Run `cd frontend && npm run test:coverage` to create `frontend/coverage/index.html`. CI runs both reports, type checks, tests, and production builds. Coverage reports identify gaps; thresholds are deliberately not yet enforced globally because framework/bootstrap code and the growing integration surface should not encourage low-value tests merely to satisfy a number.

The repository currently contains the application foundation, a working book catalogue, domain model, cart behavior foundation, authentication state foundation, sample H2 data, and automated tests. Cart HTTP workflows, registration/login, checkout, and order-summary screens are the next business-feature increments.

Catalogue metadata and stock are normalized into separate `book` and `inventory` tables.
The public book response remains unchanged and exposes availability through the inventory
aggregate.

## Technology stack

### Back end

- **Java 17 language target**: the supported Java language level for production code.
- **Spring Boot 3.3**: application bootstrapping, dependency management, and executable packaging.
- **Spring Web**: REST controllers and HTTP request handling.
- **Spring Data JPA**: repository abstraction and persistence mapping.
- **Spring Security**: the security boundary for public and authenticated API routes.
- **H2**: zero-configuration in-memory database for local development and tests.
- **MySQL Connector/J**: ready for a MySQL deployment through the `mysql` Spring profile.
- **Maven**: dependency management, compilation, testing, and packaging.
- **JUnit 5**: backend unit-test framework.
- **Mockito**: mocking framework for application-service and controller tests.

### Front end

- **React 18**: component-based user interface.
- **TypeScript**: type-safe application and domain code.
- **Vite**: local development server and production bundler.
- **Vitest**: fast TypeScript/React test runner.
- **Testing Library**: user-focused component tests.
- **jsdom**: browser-like test environment for frontend tests.

## Repository layout

```text
.
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/bookstore/
│       │   ├── api/              # REST controllers
│       │   ├── application/     # Use-case/application services
│       │   ├── domain/          # Domain entities and invariants
│       │   ├── dto/             # API request/response models
│       │   ├── exception/       # API error contract and handlers
│       │   ├── infrastructure/  # Security, CORS, correlation IDs, logging
│       │   └── repository/      # Spring Data persistence interfaces
│       └── test/java/com/bookstore/
│           └── ...               # Colocated-by-layer backend unit tests
├── frontend/
│   ├── package.json
│   └── src/
│       ├── api/client/           # REST client and client tests
│       ├── application/auth-state/ # Application state and tests
│       ├── components/book-card/ # Reusable UI component and test
│       ├── domain/types/         # Frontend domain types
│       └── pages/catalogue/      # Page and page tests
├── docs/architecture.md          # Architecture decisions and future evolution
├── docs/database-schema.md       # Current schema, relationships, and scaling guidance
├── docs/engineering-guidelines.md # Domain, transaction, logging, and TDD standards
├── .github/copilot-instructions.md # Repository-wide coding instructions
└── README.md
```

Checkout is implemented as a single database transaction. The transaction claims the
idempotency key, locks the cart and books, reserves stock, creates the order, clears
the cart, and completes the idempotency record atomically. External integrations such
as payments or fulfilment should be introduced behind dedicated ports when needed,
with an outbox for reliable event publication.

The checkout architecture also uses focused SOLID patterns:

- `Order.Builder` keeps order construction extensible without telescoping constructors.
- `PaymentStrategy` defines the payment provider contract.
- `PaymentStrategyFactory` selects a payment strategy by `PaymentMethod`.

These are application seams for future checkout work, not mock payment implementations. Concrete payment adapters will be added when the payment workflow and provider requirements are defined.

Environment-sensitive infrastructure values such as CORS origins, allowed methods, headers, and credential policy are configured under `bookstore.cors` in `application.yml` and can be overridden per Spring profile or deployment environment. Domain thresholds and protocol identifiers are represented by named constants or enums rather than scattered literals.

Business operations use structured events through `BusinessEventLogger`. Cart additions/removals and checkout lifecycle events, including idempotency replays and rejections, include stable event names and safe identifiers. Request correlation IDs are propagated through the logging context. Sensitive values such as passwords, tokens, authorization headers, payment data, and request fingerprints are never logged.

Checkout idempotency is backed by the database rather than process memory. Each authenticated user and idempotency key has one unique record containing a request fingerprint and checkout status. Repeating the same request after completion replays the original order identifier; reusing the key for different request data returns `409 IDEMPOTENCY_CONFLICT`; a concurrent request for the same key is rejected while the first checkout is processing. The claim, order, stock reservation, and cart mutation must be executed in one transaction when the checkout use case is added.

Frontend feature code and tests are colocated. For example:

```text
frontend/src/api/client/client.ts
frontend/src/api/client/client.test.ts
```

## Required tools

Install the following before starting development:

| Tool | Version | Purpose |
|---|---:|---|
| JDK | 17 or newer | Compile and run the Spring Boot application |
| Maven | 3.9+ | Build the backend and run Java tests |
| Node.js | 20+ | Run the frontend toolchain |
| npm | Included with Node.js | Install frontend dependencies and run scripts |
| Git | Current version | Source control |

The Maven project targets Java 17. A newer installed JDK, such as JDK 21, can run the project while Maven still compiles against the Java 17 target.

## Running locally

Open two terminals from the repository root.

### 1. Start the back end

```bash
mvn -pl backend spring-boot:run
```

The REST API starts at:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "service": "bookstore-backend",
  "status": "UP"
}
```

Catalogue endpoint:

```text
GET http://localhost:8080/api/v1/books
```

The existing versioned route remains available for backward compatibility:

```text
GET http://localhost:8080/api/v1/books
```

### 2. Start the front end

```bash
cd frontend
npm install
npm run dev
```

The React application starts at:

```text
http://localhost:5173
```

The frontend uses `http://localhost:8080/api/v1` by default. To use another API URL, set `VITE_API_URL` before starting Vite:

```bash
VITE_API_URL=http://localhost:8080/api/v1 npm run dev
```

On Windows PowerShell:

```powershell
$env:VITE_API_URL = "http://localhost:8080/api/v1"
npm run dev
```

## Database configuration

H2 is the default local database and requires no setup:

```text
jdbc:h2:mem:bookstore
```

The application also includes a MySQL-ready profile. Set the required environment variables and start the backend with:

```bash
mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=mysql
```

Environment variables:

```text
MYSQL_HOST=...
MYSQL_PORT=...
MYSQL_DATABASE=...
MYSQL_USERNAME=...
MYSQL_PASSWORD=...
```

For a production deployment, database migrations should be introduced before managing persistent production data.

## Secrets and Vault

No production credentials, tokens, or passwords are committed to this repository. Local H2 uses the non-production `sa` account without a password. MySQL credentials must be supplied through the environment or a secret manager; there are no committed MySQL credential defaults.

The backend includes the Spring Cloud Vault Config starter. Vault is used by the `vault` Spring profile. You do not need Vault for the default local H2 profile.

### Local development without Vault

Use the default profile. It uses an in-memory H2 database and contains no production secret:

```bash
mvn -pl backend spring-boot:run
```

This is the recommended mode for ordinary feature development. No Vault address or Vault token is required.

### Non-production with a local/dev Vault

If you want to exercise the same secret-delivery path as staging or production, run a separate development Vault instance and enable the `vault` profile. For example, after authenticating to a local Vault CLI:

```bash
vault secrets enable -path=secret kv-v2
vault kv put secret/bookstore-backend \
  BOOKSTORE_DATASOURCE_URL=jdbc:mysql://localhost:3306/bookstore \
  BOOKSTORE_DATASOURCE_USERNAME=bookstore_app \
  BOOKSTORE_DATASOURCE_PASSWORD=change-me-locally \
  BOOKSTORE_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver
```

Supply the Vault connection details only to the running process, not in source files:

```bash
VAULT_ADDR=http://127.0.0.1:8200
VAULT_TOKEN=<local-development-token>
mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=vault
```

On PowerShell, use `$env:VAULT_ADDR` and `$env:VAULT_TOKEN` instead. Use disposable credentials and a non-production Vault for this mode.

### Production or staging

The deployment platform starts the application with the `vault` profile and provides `VAULT_ADDR` plus a short-lived, least-privilege Vault credential through workload identity, AppRole, Kubernetes authentication, or the platform's secret store. Developers and application code do not read or commit the secret values. Vault supplies them to Spring during startup.

```bash
mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=vault
```

The current local configuration uses `VAULT_TOKEN` for the Vault client. In production, do not use a root token or a long-lived personal token; replace this bootstrap mechanism with the deployment platform's Vault authentication integration before exposing the service.

The Vault profile requires the following bootstrap values:

```text
VAULT_ADDR=https://vault.example.com
VAULT_TOKEN=<short-lived-token-injected-at-runtime>
```

The Vault KV engine should expose the application's database settings under the configured application context, including:

```text
BOOKSTORE_DATASOURCE_URL
BOOKSTORE_DATASOURCE_USERNAME
BOOKSTORE_DATASOURCE_PASSWORD
BOOKSTORE_DATASOURCE_DRIVER
```

The current HTTP Basic authentication loads users from the `user_account` table. Store only one-way password hashes in that table; do not store plaintext passwords in Vault or the database. Future JWT signing keys, OAuth client secrets, payment credentials, and other tokens must follow the same Vault-backed configuration pattern. The frontend API base URL is configuration, not a secret; it belongs in `VITE_API_URL`, while browser-held access tokens must never be committed or embedded in the bundle.

Vault path and context settings can be changed with `VAULT_KV_BACKEND`, `VAULT_APPLICATION_NAME`, and `VAULT_DEFAULT_CONTEXT`. In CI/CD and production, inject `VAULT_TOKEN` through the platform's workload identity or secret store rather than putting it in shell history, source code, or checked-in files. Prefer short-lived Vault tokens and least-privilege policies.

The application receives these values as configuration at startup and does not provide an endpoint for retrieving them. Do not log them, expose them through actuator endpoints, or place them in frontend code. The `vault` profile is fail-closed for database credentials: missing Vault/environment values prevent the application from starting rather than using insecure defaults.

## Testing and validation

### Backend

Run all backend unit tests:

```bash
mvn -pl backend test
```

Run the complete Maven build:

```bash
mvn clean verify
```

Backend tests cover domain invariants, availability filtering, DTO mapping, repository queries, application-service delegation, controller behavior, not-found and malformed-ID responses, exception responses, correlation IDs, and CORS configuration.

Core persisted entities record `createdAt` and `updatedAt` timestamps. Carts and orders
hold a typed `UserAccount` association while retaining `user_id` foreign keys and
indexes in the database; order status transitions update `updatedAt`.

## Authentication, authorization, and validation errors

Catalogue and health endpoints are public. Other backend endpoints require stateless HTTP Basic authentication backed by the `user_account` table. Unknown users and invalid credentials return a structured `401` response without revealing whether an account exists:

```json
{
  "status": 401,
  "error": "AUTHENTICATION_REQUIRED",
  "message": "Authentication is required to access this resource"
}
```

Authenticated users who lack permission receive a structured `403` response with the `ACCESS_DENIED` error code. Request-body, parameter, and domain validation failures return `400` with the existing `VALIDATION_ERROR` or `INVALID_REQUEST` contract. Passwords must be stored as one-way hashes, and authentication or validation errors never expose passwords, hashes, stack traces, or internal account details.

### Frontend

```bash
cd frontend
npm install
npm test
npm run typecheck
npm run build
```

Run frontend tests in watch mode during development:

```bash
npm run test:watch
```

Frontend tests cover API success/failure behavior, catalogue loading, successful rendering, empty catalogues, API failures, availability rendering, book-card rendering, and authentication state behavior.

## Engineering and TDD conventions

- Write a failing test for new domain behavior before implementing the behavior.
- Keep domain rules inside domain objects, not controllers or DTOs.
- Keep controllers focused on HTTP translation and delegate use cases to application services.
- Return DTOs from API endpoints instead of exposing JPA entities directly.
- Use constructor injection for dependencies.
- Keep API routes versioned under `/api/v1`.
- Give tests descriptive behavior-oriented names.
- Prefer unit tests without Spring context for domain and application logic.
- Use Spring MVC/security tests when HTTP, filters, CORS, or configuration behavior is being tested.
- Keep frontend tests focused on observable user behavior rather than implementation details.
- Use transaction boundaries for cart and checkout operations.
- Make checkout idempotent with a user-scoped idempotency key.
- Protect stock changes with an atomic conditional update or explicit locking/versioning strategy.
- Emit structured business logs for cart, checkout, stock, and idempotency events without sensitive data.
- Use constants, enums, configuration, and fixture files instead of magic numbers or duplicated hardcoded data.

The complete rules are documented in [`docs/engineering-guidelines.md`](docs/engineering-guidelines.md) and are also available to repository-aware coding agents in [`.github/copilot-instructions.md`](.github/copilot-instructions.md).

## Current API foundation

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/health` | Check service availability | Public |
| `GET` | `/api/v1/books` | Retrieve books currently in stock | Public |
| `GET` | `/api/v1/books/{id}` | Retrieve one book by ID | Public |

The books list supports incremental offset pagination and title search on the same
endpoint:

```text
GET /api/v1/books?offset=0&limit=5&search=clean
```

The response contains `content`, `offset`, `limit`, `hasNext`, and `total`. The
frontend debounces title searches and loads the next page automatically as the
catalogue approaches the viewport.

The domain layer models `UserAccount`, `Book`, `Cart`, `CartItem`, `Order`, and `OrderItem`.

## Authentication and cart APIs

## Interactive API documentation

Start the backend from the repository root:

```powershell
mvn -pl backend spring-boot:run
```

Then open the Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

The shorter redirect URL is also available:

```text
http://localhost:8080/swagger-ui.html
```

The canonical catalogue routes are versioned under `/api/v1/books`. Cart quantity
and removal routes use the `bookId` returned in the cart response; existing
`cartItemId` values are also accepted for compatibility.

Machine-readable OpenAPI documents:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs.yaml
```

The Swagger UI is enabled by the `springdoc-openapi-starter-webmvc-ui` dependency.
Swagger UI and OpenAPI document endpoints are public in the local configuration. Do
not expose them publicly without access control in a production deployment.

### Running the documented APIs

1. Start the backend. The default profile uses an in-memory H2 database, loads the
   sample books from `backend/src/main/resources/data.sql`, and listens on port `8080`.
2. Open the Swagger UI link above.
3. Execute `POST /api/auth/register` or `POST /api/auth/login`.
4. Copy the returned `token`.
5. Click **Authorize** in Swagger UI and enter:

   ```text
   Bearer <token>
   ```

6. Execute the authenticated cart endpoints. The cart APIs use the authenticated
   account and do not accept a user ID from the request.

The currently documented API surface is:

| Method | Endpoint | Authentication | Purpose |
|---|---|---|---|
| `GET` | `/api/v1/health` | Public | Backend health |
| `GET` | `/api/v1/books` | Public | List books currently in stock |
| `GET` | `/api/v1/books/{id}` | Public | Retrieve one book |
| `POST` | `/api/auth/register` | Public | Register and receive a JWT |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `GET` | `/api/cart` | Bearer JWT | Retrieve the current user's cart |
| `POST` | `/api/cart/items` | Bearer JWT | Add an available book |
| `PATCH` | `/api/cart/items/{bookId}` | Bearer JWT | Change quantity |
| `DELETE` | `/api/cart/items/{bookId}` | Bearer JWT | Remove an item |

Example cart request:

```http
POST http://localhost:8080/api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookId": "b55c75c7-23b7-4144-8a02-a3bccbfa045f",
  "quantity": 2
}
```

Example authentication response:

```json
{
  "token": "<jwt>",
  "email": "reader@example.com",
  "displayName": "Reader"
}
```

The JWT is stateless and expires according to `bookstore.security.jwt.expiration`
(one hour by default). `BOOKSTORE_JWT_SECRET` is required in every environment,
including local development; copy `.env.example` and replace its placeholder with a
random value of at least 32 characters. Never commit or log the secret or tokens.

Invalid request data returns the existing structured error response. Invalid login
credentials return `401` with a generic message. Duplicate registration returns
`409`. Missing or invalid bearer credentials return `401`. Cart item or book lookup
failures return `404`, and unavailable books cannot be added.

Registration and login are available under `/api/auth`:

```http
POST /api/auth/register
Content-Type: application/json

{"email":"reader@example.com","password":"StrongPass1","displayName":"Reader"}
```

```http
POST /api/auth/login
Content-Type: application/json

{"email":"reader@example.com","password":"StrongPass1"}
```

Both endpoints return a short-lived JWT response containing `token`, `email`, and
`displayName`. Send the token on protected requests as:

```text
Authorization: Bearer <token>
```

Passwords are BCrypt-hashed before persistence. Registration rejects duplicate email
addresses and weak passwords. Invalid login credentials return a generic authentication
failure.

### Password transport and verification

The browser must not hash the password as a replacement for HTTPS. A client-side hash
would act as a reusable password-equivalent and could be replayed by anyone who steals
it. The supported flow is:

1. The client sends the password in the registration or login request body.
2. The request is transported over HTTPS/TLS in staging and production, so the password
   is encrypted on the network.
3. During registration, the backend hashes the password with BCrypt and stores only the
   BCrypt hash in `user_account.password_hash`.
4. During login, Spring Security uses the submitted password and the stored BCrypt hash
   through the configured `PasswordEncoder`; the plaintext password is never persisted.
5. The backend returns a short-lived JWT. Subsequent protected requests use the JWT,
   not the password.

The password is therefore visible in the browser's own request inspector during local
development, but it is encrypted on the network when HTTPS is used. Never log request
bodies, passwords, authorization headers, or JWTs.

For local development, the default H2 profile intentionally uses HTTP:

```text
http://localhost:8080
```

For a directly TLS-terminating Spring Boot instance, provide a PKCS12 keystore and
start the HTTPS profile:

```powershell
$env:HTTPS_KEY_STORE = "file:C:\secrets\bookstore-keystore.p12"
$env:HTTPS_KEY_STORE_PASSWORD = "<keystore-password>"
$env:HTTPS_KEY_ALIAS = "bookstore"
mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=https
```

The HTTPS API is then available at:

```text
https://localhost:8443
```

In production, TLS is commonly terminated at a trusted reverse proxy or load balancer.
In that setup, configure the proxy with a valid certificate, forward requests to the
backend over a private network, and set the frontend API URL to the HTTPS origin:

```powershell
$env:VITE_API_URL = "https://api.example.com/api/v1"
npm run dev
```

Use a valid certificate issued by a trusted certificate authority in non-local
environments. Do not disable certificate verification or use self-signed certificates
for ordinary production clients.

Authenticated cart endpoints are:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/cart` | Retrieve the current user's cart |
| `POST` | `/api/cart/items` | Add an available book |
| `PATCH` | `/api/cart/items/{bookId}` | Change an item's quantity |
| `DELETE` | `/api/cart/items/{bookId}` | Remove an item |

Cart ownership is derived from the authenticated account; clients cannot provide an
arbitrary user ID. Cart quantities are validated in the domain, repeated additions
merge into one line, and totals are calculated from persisted book prices. Checkout,
checkout is implemented with persisted order snapshots, transactional cart clearing and
database-backed idempotency. Payment capture remains intentionally out of scope.

### Catalogue response example

```json
[
  {
    "id": "b55c75c7-23b7-4144-8a02-a3bccbfa045f",
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "price": 39.99,
    "stockQuantity": 8,
    "inStock": true
  }
]
```

The catalogue endpoint returns only books with `inStock: true`. A request for an unknown book ID returns `404` with the centralized error contract:

```json
{
  "timestamp": "2026-09-19T15:04:28.456260Z",
  "correlationId": "eab30791-f014-43c4-bbdc-9635bbeeb06c",
  "status": 404,
  "error": "BOOK_NOT_FOUND",
  "message": "Book not found: 00000000-0000-0000-0000-000000000000",
  "path": "/api/v1/books/00000000-0000-0000-0000-000000000000"
}
```

## Sample book data

The default H2 profile loads the following books from `backend/src/main/resources/data.sql`:

| Title | Author | Price | Stock |
|---|---|---:|---:|
| Clean Code | Robert C. Martin | 39.99 | 8 |
| The Pragmatic Programmer | Andrew Hunt and David Thomas | 49.99 | 5 |
| Domain-Driven Design | Eric Evans | 59.99 | 0 |

The out-of-stock book is persisted for availability behavior but is intentionally excluded from the catalogue response.

## Architecture summary

The application is intentionally a modular monolith rather than a group of microservices. This keeps local development, transactions, and testing straightforward while preserving explicit boundaries for future growth.

- **API layer** handles HTTP concerns.
- **Application layer** coordinates use cases.
- **Domain layer** protects business invariants.
- **Repository layer** abstracts persistence.
- **Infrastructure layer** provides security, CORS, correlation IDs, and logging.
- **DTOs** keep the HTTP contract separate from persistence entities.

See [`docs/architecture.md`](docs/architecture.md) for the detailed architecture decisions and scalability considerations.
