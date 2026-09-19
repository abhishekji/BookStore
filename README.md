# Simple Online Bookstore

This project is a code kata for building a simple online bookstore with a React front end and a Spring Boot RESTful back end.

The target user journey is:

1. Display a catalogue of books.
2. Add books to a shopping cart.
3. Display the cart contents.
4. Increase or decrease item quantities.
5. Remove books from the cart.
6. Continue toward checkout and order summary workflows.

The repository currently contains the application foundation, catalogue endpoint, domain model, cart behavior, authentication state foundation, and automated tests. Cart HTTP workflows, registration/login, checkout, and order-summary screens are the next business-feature increments.

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
└── README.md
```

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
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=bookstore
MYSQL_USERNAME=bookstore
MYSQL_PASSWORD=bookstore
```

For a production deployment, database migrations should be introduced before managing persistent production data.

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

Backend tests cover domain invariants, DTO mapping, application-service delegation, controller behavior, exception responses, correlation IDs, and CORS configuration.

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

Frontend tests cover API success/failure behavior, catalogue rendering, book-card rendering, and authentication state behavior.

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

## Current API foundation

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| `GET` | `/api/v1/health` | Check service availability | Public |
| `GET` | `/api/v1/books` | Retrieve the book catalogue | Public |

The domain layer already models `UserAccount`, `Book`, `Cart`, `CartItem`, `Order`, and `OrderItem`. Business endpoints for registration, login, cart management, checkout, and orders should be added incrementally behind application services.

## Architecture summary

The application is intentionally a modular monolith rather than a group of microservices. This keeps local development, transactions, and testing straightforward while preserving explicit boundaries for future growth.

- **API layer** handles HTTP concerns.
- **Application layer** coordinates use cases.
- **Domain layer** protects business invariants.
- **Repository layer** abstracts persistence.
- **Infrastructure layer** provides security, CORS, correlation IDs, and logging.
- **DTOs** keep the HTTP contract separate from persistence entities.

See [`docs/architecture.md`](docs/architecture.md) for the detailed architecture decisions and scalability considerations.
