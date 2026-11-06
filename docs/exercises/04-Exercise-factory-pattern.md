# Exercise 04 — Factory Pattern

> **Sprint 4** of `start.md`. Builds on Exercise 03. Numbering note:
> `01-Exercise-build-the-be.md` (Spring Boot) comes *after* 02–05.

## Goal

Make `AccountFactory` actually do its job: be the **single place** that decides which concrete
`Account` subtype is created and which id it gets. `Bank` should be able to offer three products
without ever mentioning a concrete account class.

## Prerequisites / current state

Read before you start:

- `src/main/java/ch/bbw/AccountFactory.java`
- `src/main/java/ch/bbw/Bank.java`

### What already exists

`AccountFactory` is already in place and already owns two responsibilities:

- **id generation** — a `nextAccountId` counter starting at `1000`, with a type prefix:
  `S-` savings, `Y-` promo youth, `P-` salary;
- **instantiation** — three `create…()` methods.

`Bank` holds one `AccountFactory`, calls it in `createSavingsAccount()`,
`createPromoYouthSavingsAccount()` and `createSalaryAccount(long)`, and puts the result into its
`TreeMap`. That part of the wiring is already correct.

### What is broken

Look at `AccountFactory.java` lines 18 and 24:

```java
public Account createPromoYouthSavingsAccount() {
    String id = "Y-" + nextAccountId++;
    return new SavingsAccount(id);      // <-- wrong type
}

public Account createSalaryAccount(long creditLimit) {
    String id = "P-" + nextAccountId++;
    return new SavingsAccount(id);      // <-- wrong type, and creditLimit is discarded
}
```

All three products return a plain `SavingsAccount`. The prefix is the only thing that differs, so
"account type" is currently encoded in a **string**, not in the type system. `creditLimit` is
accepted and silently dropped.

After Exercise 03 the right classes exist, so this is now fixable.

## Work test-first (TDD)

Task 1 is a regression test for the exact bug shown above (`createSalaryAccount` silently dropping
`creditLimit`) plus the contract the rest of the factory must hold.

1. Write the `instanceof`/prefix/uniqueness tests from Task 1 first, against the **current**, still
   broken `AccountFactory`.
2. Run `./mvnw test` and watch the `createSalaryAccount`/`createPromoYouthSavingsAccount` tests fail
   — that failure *is* the bug from "What is broken" above, now pinned down in a test instead of just
   described in prose.
3. Fix one method at a time (Task 2) until its test goes green, then move on to `AccountType` (Task
   3) and the rest.

## Tasks

### 1. Tests

Add `src/test/java/ch/bbw/AccountFactoryTests.java`:

- each `create…()` returns an instance of the **expected concrete class** (this is one of the few
  legitimate uses of `instanceof` — you are testing the factory's contract);
- each id starts with the expected prefix;
- ids are unique over e.g. 1000 creations;
- `createSalaryAccount(-50_000)` produces an account whose credit limit is actually `-50_000`
  (the regression test for the discarded-parameter bug);
- `createSalaryAccount(+50_000)` is rejected.

Also implement the `testCreate()` stub in `src/test/java/BankTests.java`: create two savings
accounts through `Bank` and assert that both ids are non-null, different, and resolvable via
`Bank.getAccount(id)`.

### 2. Fix the three creation methods

Return the correct subtype from each method, and pass `creditLimit` into the `SalaryAccount`
constructor. This is a small change — but note that it was **only possible in one file**. That is
the payoff of having a factory at all: `Bank` needed no edit.

### 3. Introduce an `AccountType`

Replace the three ad-hoc prefixes with an enum that keeps the type and its prefix together:

```java
public enum AccountType {
    SAVINGS("S"),
    PROMO_YOUTH("Y"),
    SALARY("P");
    // ...
}
```

Now the prefix cannot drift away from the type, and you can iterate over all products.

### 4. Simple factory vs. factory method

Add a single dispatching entry point next to (or instead of) the three methods:

```java
public Account create(AccountType type, long creditLimit) { ... }
```

Implement it with a `switch` over `AccountType`. Then answer, in a comment or your commit message:

- Which of the two APIs would you expose to `Bank` — three named methods, or one `create(type, …)`?
- The `creditLimit` parameter is meaningless for savings accounts. What does that tell you about the
  single-method design? How could you avoid the useless parameter (overloads, a small parameter
  object, `Optional`)?

This is the difference between a **simple factory** (one class with a `switch`, what you have here)
and the **Factory Method pattern** (each product has its own creator subclass). Note which one this
exercise actually implements.

### 5. Make id generation robust

The counter is the factory's second responsibility. Tighten it:

- The counter is currently a plain `long` incremented with `++`, which is **not** thread-safe. Two
  concurrent calls can hand out the same id. Use `AtomicLong`, or document explicitly that the
  factory is single-threaded (it will not be once Spring serves HTTP requests in Exercise 01).
- Should the counter be shared across all types (today: `S-1000`, `Y-1001`, `P-1002`) or per type
  (`S-1000`, `Y-1000`, `P-1000`)? Both are defensible — pick one and state why.
- Add a test that ids are unique across many creations.

### 6. Keep the factory the only place that says `new`

Grep the project: `new SavingsAccount(`, `new SalaryAccount(`, `new PromoYouthSavingsAccount(` must
appear only inside `AccountFactory` (and in tests, where constructing a subtype directly is fine).

`Bank` must not construct accounts, and must not import `ch.bbw.accounts.SavingsAccount` at all —
only the abstract `ch.bbw.accounts.Account`.

## Discussion questions

- What would `Bank` look like without a factory? Write the three methods out with `new` inline and
  compare. What exactly did the factory buy you, and what did it cost?
- The factory returns the abstract type `Account`. Why not return `SalaryAccount` from
  `createSalaryAccount()` — that would be more precise. What do you gain by being vaguer?
- Where does the id-generation responsibility *really* belong: the factory, the `Bank`, or the
  `Account` constructor? Argue for one. (Hint: what happens to the counter when the bank restarts,
  once Exercise 01's persistence arrives?)
- The Spring version of this project (Exercise 01) can create beans for you. Does a `@Service`
  replace the factory, or complement it? Park the answer for Sprint 12.

## Out of scope for this exercise

- Making `Bank` a singleton — Exercise 05.
- Persisting the id counter.
- HTTP, DTOs, Spring.

## Acceptance criteria

- [ ] `createPromoYouthSavingsAccount()` and `createSalaryAccount(long)` return their own subtypes.
- [ ] `creditLimit` reaches the `SalaryAccount` and is verified by a test.
- [ ] Account type and id prefix are defined in one place (`AccountType`), not as scattered string
      literals.
- [ ] Id generation is thread-safe, or its single-threaded assumption is documented.
- [ ] `Bank` does not import or instantiate any concrete account class.
- [ ] `./mvnw test` passes, including the new `AccountFactoryTests`.

## Stretch goals (optional)

- Add a fourth product (e.g. `BusinessAccount`) and count how many files you had to touch. That
  number is the real measure of the pattern.
- Make the factory configurable: pass the starting id into the constructor instead of hard-coding
  `1000`, so tests can pin the ids they expect.
- Replace the `switch` with a `Map<AccountType, Supplier<Account>>` registry and discuss the
  trade-off against the `switch`.
