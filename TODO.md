# Progress checklist

Personal tracking file — what's actually done in the code vs. what each exercise still asks for.
Last checked against the code: 2026-09-13 (all 19 tests still `fail("ToDo")`,
`./mvnw test` → `Tests run: 19, Failures: 19, Errors: 0`).

Update this file whenever you finish a task below — that's the whole point of it.

Work **test-first**: each exercise brief now lists its tests as Task 1, before any implementation
task. Write the test, watch it fail (`./mvnw test`), then implement just enough to pass it.

---

## Sprint 1 — Baseline

- [x] `Bank` + `Account`: create, deposit, withdraw, read balance all work.

## Sprint 2 — Bookings ([`docs/exercises/02-Exercise-bookings.md`](docs/exercises/02-Exercise-bookings.md))

- [x] Tests written first: `BookingTests.testInitialization/testPrint`, `AccountTests.testDeposit/testWithdraw/testPrint/testMonthlyPrint`.
- [x] `Booking` extended to `(date, amount, text)`; redundant compact-constructor/accessor code removed.
- [x] `Account.deposit(...)`/`withdraw(...)` (+ `Bank` equivalents) accept a text, with 2-arg overloads kept as defaults.
- [x] `Account.getBookings()` added, returns an unmodifiable view.
- [x] `balance` field removed from `Account`; `getBalance()` derived from the booking list.
- [x] `SavingsAccount.withdraw(...)` re-checked: still reads balance *before* delegating — verify overdraft check still works after balance is derived.
- [x] Booking date semantics fixed (code and Javadoc agree — ms since epoch, or bank-days, pick one).
- [x] `printStatement()` (or similar) added to `Account`.

## Sprint 3 — Specific accounts ([`docs/exercises/03-Exercise-specific-accounts.md`](docs/exercises/03-Exercise-specific-accounts.md))

- [ ] Tests written first: `SalaryAccountTests`, `SavingsAccountTests`, new `PromoYouthSavingsAccountTests` (they won't compile until the classes below exist — that's expected).
- [ ] `ch.bbw.accounts.SalaryAccount` created (`creditLimit`, overdraft-to-limit rule, rejects positive limit).
- [ ] `ch.bbw.accounts.PromoYouthSavingsAccount` created, with its own rule (withdrawal cap / bonus rate / minimum balance) and a documented superclass choice.
- [ ] `Account` gets a shared abstract operation (e.g. `getAccountType()` or `applyInterest(...)`) that every subtype implements.
- [ ] `Bank` verified to stay polymorphic — no `instanceof`, no cast to a concrete account type.

## Sprint 4 — Factory pattern ([`docs/exercises/04-Exercise-factory-pattern.md`](docs/exercises/04-Exercise-factory-pattern.md))

- [x] Id generation with type prefixes (`S-`/`Y-`/`P-`) and a counter starting at 1000 — *this is the exercise's own starting point, not something to redo.*
- [ ] Tests written first: `src/test/java/ch/bbw/AccountFactoryTests.java` (should fail against the current buggy factory), `BankTests.testCreate()`.
- [ ] **Bug still open:** `createPromoYouthSavingsAccount()` and `createSalaryAccount(long)` in `AccountFactory` still both return a plain `SavingsAccount` — fix to return the real subtypes (needs Sprint 3 done first).
- [ ] `creditLimit` actually passed into `SalaryAccount`'s constructor instead of discarded.
- [ ] `AccountType` enum introduced (type + prefix together).
- [ ] Single dispatching `create(AccountType, long)` entry point added, with a documented answer to the API-choice question.
- [ ] Id counter made thread-safe (`AtomicLong`) or the single-threaded assumption documented.
- [ ] `Bank` confirmed to not import/instantiate any concrete account class.

## Sprint 5 — Singleton ([`docs/exercises/05-Exercise-singleton-pattern.md`](docs/exercises/05-Exercise-singleton-pattern.md))

- [ ] Tests written first: identity (`assertSame`), constructor-not-public (reflection), state-isolation test (see the brief's note on why this one may pass trivially at first).
- [ ] `Bank` constructor made `private`; `public static Bank getInstance()` added.
- [ ] All call sites (`new Bank()` → `Bank.getInstance()`) updated, including existing tests.
- [ ] Eager vs. lazy-synchronized vs. other implementation compared and one chosen, with reasoning.

## Sprints 6–11 — Spring Boot backend ([`docs/exercises/10-Exercise-build-the-be.md`](docs/exercises/10-Exercise-build-the-be.md))

- [ ] Not started — comes after Sprint 5.

## Untracked / extra work already in the code

Not requested by any exercise above yet, has no test coverage — decide whether to keep, and add
tests for it when its sprint comes around (or now, if you want to lock in the behaviour):

- [ ] `Bank.top5HighestBalances()` / `top5LowestBalances()` — matches the `05_UML_Bank5.pdf`
  scenario (bank staff sorting accounts by balance), untested.

---

## How to re-check this file's accuracy

```bash
./mvnw test   # should read "Tests run: 19, Failures: 19" on a fresh checkout — anything less means progress
```

Grep for stub markers to see which test methods still need writing:

```bash
grep -rn 'fail("ToDo")' src/test
```
