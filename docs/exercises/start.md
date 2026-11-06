# Bank Exercise Start

This project is a start point for students.  
The focus is on **use-cases** and **sprints**: in each step, one new part is designed and implemented.

At the moment, the model includes:
- `Bank`
- `Account`

> Before starting a sprint, read its requirement scenario PDF in
> [`docs/scenarios/`](../scenarios/01_UML_Bank2.pdf) and draft your own use-case from it — see
> [README: Scenarios](../../README.md#scenarios-from-requirement-to-use-case-to-code) for the
> PDF-to-sprint mapping and workflow. The tasks below are the distilled version; the scenario is
> the raw requirement.
>
> **Work test-first (TDD).** Every exercise brief (`02-Exercise-*.md` onward) lists its **Tests**
> as Task 1, before any implementation task. Write those tests against the target API described in
> the brief, watch them fail (`./mvnw test` — red, often a compile error because the class or method
> doesn't exist yet), then write only enough code to make each one pass (green), refactoring as you
> go. Do not start an implementation task before its test is written and failing.

## Suggested Sprint Sequence

1. **Sprint 1 (Baseline)**  
   Implement `Bank` + `Account` with core operations: create account, deposit, withdraw, and read balance.

2. **Sprint 2 (Bookings)**  
   Add `Booking` (e.g., date, amount, text).  
   Store bookings in `Account` and derive the account balance from the bookings.

3. **Sprint 3 (Specific Accounts / Inheritance)**  
   Introduce account subtypes (for example `SavingsAccount`, `SalaryAccount`).  
   Implement specific rules per subtype (fees, limits, interest, etc.).

4. **Sprint 4 (Factory Pattern)**  
   Add an `AccountFactory` to centralize account creation and type selection.

5. **Sprint 5 (Singleton Pattern)**  
   Make `Bank` a singleton to ensure only one bank instance exists, and discuss trade-offs.

## Extension: Spring Boot Backend with API Service

6. **Sprint 6 (Spring Boot Setup)**  
   Create a Spring Boot project and move the domain model into a backend structure (`model`, `service`, `controller`).

7. **Sprint 7 (REST API for Accounts)**  
   Expose endpoints for account creation, deposits, withdrawals, and balance retrieval.

8. **Sprint 8 (REST API for Bookings)**  
   Expose endpoints to create and list bookings per account, and return account history.

9. **Sprint 9 (Persistence with Spring Data JPA)**  
   Persist accounts and bookings using JPA (for example with H2 or PostgreSQL), including relationships.

10. **Sprint 10 (Validation and Error Handling)**  
    Add request validation and consistent API error responses (HTTP status codes + error payload).

11. **Sprint 11 (API Tests)**  
    Add service and controller tests (unit/integration) for core use-cases.

12. **Sprint 12 (Architecture Discussion)**  
    Compare classic OO patterns (Factory, Singleton) with Spring mechanisms (DI container, bean scopes).
