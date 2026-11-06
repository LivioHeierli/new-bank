# Exercise 01 — Build the Backend

## Goal

Turn the existing `ch.bbw` domain model (`Bank`, `Account`, `AccountFactory`, `Booking`, `Scheduled`)
into a working Spring Boot REST API that a separate frontend (Vite/JS, built in a later exercise)
can talk to over HTTP.

You are **not** starting from scratch — the domain logic already exists and is tested
(`src/test/java/BankTests.java`, `BookingTests.java`, `src/test/java/ch/bbw/accounts/*`).
This exercise is about wrapping that domain model in a web layer, not rewriting it.

## Prerequisites / current state

- Spring Boot project (`pom.xml`), Java 21, Maven.
- Domain classes under `src/main/java/ch/bbw`:
  - `Bank` — in-memory registry of `Account`s (keyed by account id), exposes create/deposit/withdraw/balance operations.
  - `Account` (abstract) / `SavingsAccount` — balance + booking history.
  - `AccountFactory` — creates accounts with generated ids (`S-…`, `Y-…`, `P-…`).
  - `Booking` — immutable record of a single transaction (date, amount).
  - `Scheduled` — represents a transaction date, rejects past-due dates.
  - `exceptions.InvalidAmountException`, `exceptions.InvalidDateException`.
- `pom.xml` currently only has `spring-boot-devtools` and `junit-jupiter-engine` — **no web starter yet**.
- No REST controllers, no DTOs, no persistence exist yet.

### Known bug to fix as part of this exercise

`AccountFactory.createPromoYouthSavingsAccount()` and `AccountFactory.createSalaryAccount(long)`
both instantiate a plain `SavingsAccount` instead of a dedicated subclass. Decide whether to:

- introduce `PromoYouthSavingsAccount` / `SalaryAccount` subclasses with their own rules (e.g. salary
  account should allow going into its `creditLimit`), or
- explicitly document that all account types share `SavingsAccount` behaviour for now and drop the
  unused `creditLimit` parameter.

Either is acceptable — pick one, note the decision in your PR/commit message.

## Tasks

### 1. Add the web starter

Add `spring-boot-starter-web` to `pom.xml` (compile scope, not test). Confirm the app boots with
`./mvnw spring-boot:run` and serves on `localhost:8080`.

### 2. Design the REST API

Expose the `Bank` operations as HTTP endpoints. Suggested resource shape (adjust as needed, but keep
it RESTful):

| Method | Path                          | Body                          | Response                          |
|--------|-------------------------------|--------------------------------|------------------------------------|
| POST   | `/accounts/savings`           | —                               | `{ "id": "S-1000" }`               |
| POST   | `/accounts/promo-youth`       | —                               | `{ "id": "Y-1000" }`               |
| POST   | `/accounts/salary`            | `{ "creditLimit": -500 }`       | `{ "id": "P-1000" }`               |
| GET    | `/accounts/{id}`              | —                               | `{ "id", "balance" }`              |
| GET    | `/accounts/{id}/balance`      | —                               | `{ "balance": 12345 }`             |
| POST   | `/accounts/{id}/deposit`      | `{ "amount": 1000, "date": … }` | `{ "balance": … }`                 |
| POST   | `/accounts/{id}/withdraw`     | `{ "amount": 500, "date": … }`  | `{ "balance": … }`                 |
| GET    | `/bank/balance`                | —                               | `{ "balance": … }`                 |
| GET    | `/bank/top5/highest`          | —                               | `[ { "id", "balance" }, … ]`       |
| GET    | `/bank/top5/lowest`           | —                               | `[ { "id", "balance" }, … ]`       |

Decide what "date" means over HTTP — the domain uses `Scheduled` (relative milliseconds-until-due).
The simplest option: accept an ISO-8601 timestamp (or omit the field and default to `Scheduled.now()`
server-side) and convert it to `Scheduled` inside the controller/service — don't leak `Scheduled`'s
internal representation into the API.

### 3. Add a service layer

Don't call `Bank` directly from `@RestController`. Add a thin `@Service` (e.g. `BankService`) that
wraps a single `Bank` instance (bean-scoped singleton is fine for this exercise — no persistence
required yet) and is what the controller talks to. This keeps the web layer swappable later.

### 4. DTOs, not domain objects, cross the HTTP boundary

Create request/response records (e.g. `AccountResponse`, `BalanceResponse`, `DepositRequest`,
`WithdrawRequest`) under a `web` or `dto` package. Never return `Account` or `Booking` directly from
a controller — they're internal domain types.

### 5. Error handling

`Bank`, `Account` and friends throw `InvalidAmountException` / `InvalidDateException` /
`IllegalArgumentException`. Add a `@RestControllerAdvice` that maps these to sensible HTTP status
codes (e.g. `404` for unknown account id, `400` for invalid amount/date) with a JSON error body
(`{ "error": "message" }`) instead of a raw stack trace / default Spring error page.

### 6. CORS

The frontend (Vite dev server) will run on a different origin (typically `localhost:5173`). Add a
CORS configuration (`@CrossOrigin` on the controller, or a global `WebMvcConfigurer`) allowing that
origin for local development.

### 7. Tests

- Keep the existing domain tests green (`./mvnw test`).
- Add at least one `@SpringBootTest` / `@WebMvcTest` covering the controller layer: creating an
  account, depositing, withdrawing, and the error path (e.g. withdrawing more than the balance from
  a `SavingsAccount` should surface as a 4xx response, not a 500).

## Out of scope for this exercise

- Persistence (database) — an in-memory `Bank` is fine for now.
- Authentication/authorization.
- The frontend itself (covered in a later exercise).

## Acceptance criteria

- [ ] `./mvnw spring-boot:run` starts the app and it responds on `localhost:8080`.
- [ ] All endpoints listed above (or your adjusted equivalents) work via `curl`/Postman.
- [ ] Domain exceptions are translated into JSON error responses with appropriate status codes, no
      raw 500s for expected error cases (invalid amount, invalid date, unknown account id).
- [ ] CORS is configured for the local Vite origin.
- [ ] `AccountFactory` bug (see above) is fixed or explicitly documented as a deliberate decision.
- [ ] `./mvnw test` passes, including new controller-level tests.

## Stretch goals (optional)

- Add a `GET /accounts/{id}/bookings` endpoint returning the transaction history.
- Add OpenAPI/Swagger UI (`springdoc-openapi-starter-webmvc-ui`) for interactive API docs.
- Add input validation (`jakarta.validation`) on request DTOs instead of hand-rolled checks.    