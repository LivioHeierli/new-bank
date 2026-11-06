 # Exercise 03 — Specific Accounts (Inheritance)

> **Sprint 3** of `start.md`. Builds on Exercise 02. Numbering note:
> `01-Exercise-build-the-be.md` (Spring Boot) comes *after* 02–05.

## Goal

Turn the single concrete account type into a small **type hierarchy**. Each subtype gets its own
withdrawal rule, and the rest of the system keeps working unchanged because everything still talks
to the abstract `Account`.

This is the exercise where polymorphism has to earn its keep: if `Bank` needs an `if (account
instanceof SalaryAccount)` anywhere, the design has failed.

## Prerequisites / current state

Read before you start:

- `src/main/java/ch/bbw/accounts/Account.java`
- `src/main/java/ch/bbw/accounts/SavingsAccount.java`
- `src/main/java/ch/bbw/Bank.java`

### What already exists

- `Account` is **abstract**: it owns the id, the booking list, and the generic `deposit()` /
  `withdraw()` implementations plus the shared validation (amount must be `> 0`, date must not be
  past due).
- `SavingsAccount extends Account` and overrides `withdraw(...)` to add one rule: **no overdraft**.
  It throws `InvalidAmountException` if `getBalance() < amount`, otherwise delegates to
  `super.withdraw(...)`.
- `Bank` already advertises three products — `createSavingsAccount()`,
  `createPromoYouthSavingsAccount()`, `createSalaryAccount(long creditLimit)` — and stores everything
  in a `TreeMap<String, Account>`, i.e. it only ever sees the abstract type.

### What is missing

1. **Two of the three products do not exist as types.** `PromoYouthSavingsAccount` and
   `SalaryAccount` are advertised by `Bank` but there is no class for either. Today all three
   products are plain `SavingsAccount` instances that differ only in their id prefix.
2. **`creditLimit` is accepted and thrown away.** `Bank.createSalaryAccount(long creditLimit)` takes
   the parameter and nothing ever reads it.
3. **`SalaryAccountTests` is an orphan.** `src/test/java/ch/bbw/accounts/SalaryAccountTests.java`
   tests a class that does not exist yet.

> The wiring in `AccountFactory` is deliberately **not** part of this exercise — you will fix that in
> Exercise 04. Here you build the types; there you build the creation.

## Work test-first (TDD)

Task 1 below asks you to write the tests for `SalaryAccount` and `PromoYouthSavingsAccount` — classes
that do not exist yet. That's intentional:

1. Write one test for one boundary described in Task 1 (e.g. "withdrawing down to exactly
   `creditLimit` succeeds").
2. Run `./mvnw test`. It should fail to compile, because `SalaryAccount` doesn't exist. That's a
   valid **red** — it tells you exactly what type and constructor signature to create next.
3. Write the smallest amount of Task 2/3's design that makes the test compile and pass
   (**green**) — not the whole class, just enough for this one test.
4. Repeat for the next boundary, refactoring as the class fills in.

Resist writing `SalaryAccount`'s full `withdraw()` logic before a test demands each piece of it.

## Tasks

### 1. Tests

- `src/test/java/ch/bbw/accounts/SalaryAccountTests.java` — implement the existing stub.
- `src/test/java/ch/bbw/accounts/SavingsAccountTests.java` — implement the existing stub.
- Add `src/test/java/ch/bbw/accounts/PromoYouthSavingsAccountTests.java`.

Cover the **boundaries**, not just the happy path:

- `SavingsAccount`: withdrawing exactly the balance succeeds; one Millirappen more throws.
- `SalaryAccount`: withdrawing down to exactly `creditLimit` succeeds; one more throws; the balance
  is genuinely negative afterwards.
- `SalaryAccount` with `creditLimit = 0` behaves like a savings account.
- A rejected withdrawal leaves the booking list untouched (this is where a badly ordered override
  bites).
- `PromoYouthSavingsAccount`: your promo rule triggers at its boundary.
- Polymorphism: put one of each type into a `List<Account>`, sum the balances, and assert the total.

### 2. `SalaryAccount` — overdraft up to a limit

Create `ch.bbw.accounts.SalaryAccount extends Account`.

- Constructor: `SalaryAccount(String id, long creditLimit)`.
- `creditLimit` is a **negative** number (or zero): it is the lowest balance the account may reach.
  Reject a positive credit limit in the constructor with `IllegalArgumentException` — a "limit" that
  lets you go *up* is nonsense.
- Override `withdraw(...)`: allow the withdrawal only while
  `getBalance() - amount >= creditLimit`. Otherwise throw `InvalidAmountException` with a message
  that names the limit.
- Add a getter for the credit limit.

Note what you did *not* have to write: amount validation, date validation, appending the booking.
That all lives in `Account`. Your override is three lines of rule plus a `super` call.

### 3. `PromoYouthSavingsAccount` — a savings account with a promo rule

Create `ch.bbw.accounts.PromoYouthSavingsAccount`.

First decide the **superclass**, and justify it in a comment:

- `extends SavingsAccount` — you inherit the no-overdraft rule for free, but you are now coupled to
  `SavingsAccount`'s implementation.
- `extends Account` — independent, but you re-implement the no-overdraft check.

Then give it at least one rule of its own. Pick one (or invent your own):

- a **withdrawal cap** — at most e.g. 200 CHF (= `20_000_000` Millirappen) may be withdrawn per
  calendar month, computed from the booking history you exposed in Exercise 02;
- a **bonus interest rate** the other accounts do not get;
- a **minimum balance** that must remain on the account.

### 4. Give the hierarchy a shared abstract operation

Add something to `Account` that every subtype must answer, so the abstract class is abstract for a
reason beyond "cannot be instantiated". Suggestions:

```java
public abstract String getAccountType();   // "Sparkonto", "Lohnkonto", "Promo-Jugendsparkonto"
```

or, more interesting:

```java
public abstract long applyInterest(Scheduled date) throws ...;  // books the interest, returns it
```

If you choose `applyInterest`, each subtype books an `INTEREST`/`ZINS` booking with its own rate,
and `Bank` can run it over **all** accounts with a single loop — no type checks.

### 5. Keep `Bank` polymorphic

`Bank` must not change much, and specifically must not learn about the new types. Verify that:

- `getBalance()` still sums over `Account::getBalance` and returns the right total for a mixed bank;
- `top5HighestBalances()` / `top5LowestBalances()` work across mixed types;
- a `SalaryAccount` in overdraft (negative balance) correctly drags the bank total down and shows up
  in `top5LowestBalances()`.

If you added `applyInterest` in task 4, add `Bank.applyInterestToAll(Scheduled date)` and note how
short it is.

## Discussion questions

- `SavingsAccount.withdraw(...)` **narrows** what the method accepts: calls that `Account` would
  have honoured are now rejected. Does that violate the **Liskov Substitution Principle**? Is
  `Account` an honest abstraction if some subclasses refuse valid-looking requests?
- `Account`'s fields are `private`, so subclasses must go through `getBalance()`. Would `protected`
  fields be more convenient? What would you give up?
- `PromoYouthSavingsAccount extends SavingsAccount` is inheritance for **code reuse**. When does
  that turn into a problem, and what is the alternative? (Keywords: composition over inheritance,
  strategy pattern.)
- Should `Account` be an abstract class or an interface (plus a shared base)? What does the abstract
  class buy you here?

## Out of scope for this exercise

- `AccountFactory` — Exercise 04.
- Making `Bank` a singleton — Exercise 05.
- HTTP, DTOs, persistence.

## Acceptance criteria

- [ ] `SalaryAccount` and `PromoYouthSavingsAccount` exist under `ch.bbw.accounts`.
- [ ] `SalaryAccount` honours its `creditLimit` on withdrawal and rejects a positive limit.
- [ ] `PromoYouthSavingsAccount` has at least one documented rule of its own.
- [ ] `Account` declares at least one abstract operation the subtypes implement.
- [ ] `Bank` contains **no** `instanceof` and no cast to a concrete account type.
- [ ] `./mvnw test` passes, including boundary tests for every subtype.

## Stretch goals (optional)

- Make `Account` a `sealed` class permitting exactly the three subtypes (Java 21) and discuss what
  that changes for `switch` pattern matching.
- Add a monthly fee (`Kontoführungsgebühr`) that only `SalaryAccount` charges, booked as a negative
  booking with text `"Gebühr"`.
- Add `toString()` to each account type and use it in the bank statement.
