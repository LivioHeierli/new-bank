# Exercise 05 — Singleton Pattern

> **Sprint 5** of `start.md`. Builds on Exercise 04, and is the last exercise before the Spring Boot
> extension in `01-Exercise-build-the-be.md` (Sprints 6–11).

## Goal

Make `Bank` a **singleton** — one bank instance for the whole application — implement it correctly
(private constructor, lazy and thread-safe), and then take the pattern seriously enough to see what
it costs you. The trade-off discussion is not an afterthought here; it is the actual learning goal,
and it sets up Sprint 12.

## Prerequisites / current state

Read before you start:

- `src/main/java/ch/bbw/Bank.java`
- your tests from Exercises 02–04

### What already exists

- `Bank` has a **public no-arg constructor**. Anyone can write `new Bank()` and get a second,
  completely independent bank with its own `TreeMap` and its own `AccountFactory`.
- `Bank` creates its own `AccountFactory` in the constructor — the dependency is hard-wired, not
  injected.
- Every test you have written so far probably starts with `var bank = new Bank();` and relies on
  that fresh, empty state.

That last point is the whole exercise in miniature: the moment `Bank` becomes a singleton, "fresh,
empty state" stops being free.

## Work test-first (TDD)

Task 1's identity and reflection tests are written against the **current** `Bank` — they fail
immediately (red), because `getInstance()` doesn't exist yet and the constructor is still public.
Write them first, then do just enough of Task 2 to turn them green.

The isolation test in Task 1 is a special case: it may *pass trivially* before Task 2, because every
test today gets its own fresh `new Bank()`. That's fine — keep it. It turns into a real (red) test
the moment the singleton lands in Task 2, which is exactly what Task 4 asks you to fix. TDD doesn't
always mean "red before any code exists" — sometimes a test only starts earning its keep once a
later change would otherwise break it silently.

## Tasks

### 1. Tests

- Assert that `Bank.getInstance() == Bank.getInstance()` (identity, use `assertSame`).
- Assert the constructor is not publicly accessible — e.g. via reflection,
  `Bank.class.getConstructor()` should throw `NoSuchMethodException`.
- Verify state isolation between tests works (deposit in one test, assert a zero balance in another,
  and make sure the order does not matter).
- Keep all of Exercises 02–04's tests green.

### 2. Make `Bank` a singleton

Convert `Bank`:

- make the constructor `private`;
- add a `public static Bank getInstance()`;
- update every call site (`new Bank()` → `Bank.getInstance()`).

### 3. Compare three implementations

Write all three (in scratch files or as comments) before you commit to one:

**(a) Eager**

```java
private static final Bank INSTANCE = new Bank();
public static Bank getInstance() { return INSTANCE; }
```

**(b) Lazy with synchronisation**

```java
private static Bank instance;
public static synchronized Bank getInstance() {
    if (instance == null) instance = new Bank();
    return instance;
}
```

**(c) Initialization-on-demand holder idiom**

```java
private static class Holder {
    static final Bank INSTANCE = new Bank();
}
public static Bank getInstance() { return Holder.INSTANCE; }
```

Answer for each: is it thread-safe, is it lazy, and what does it cost on every call?

Then explain why the classic "double-checked locking" version is **wrong** without `volatile`, and
what the JVM is allowed to do that breaks it.

Pick (c) or (a) and justify the choice in a comment.

### 4. Feel the pain in the tests

Run `./mvnw test`. Tests that were independent are now sharing one mutable bank, and results may
depend on execution order.

Fix it, and be honest about what the fix is:

- Add a `static void reset()` (or make the accounts map clearable) **for testing only**, and call it
  from a `@BeforeEach`.
- Write down why a `reset()` method that exists purely so tests can undo a design decision is a
  **code smell**, not a solution.

Also check: does JUnit run your test classes in parallel? If not today, what would break if it did?

### 5. Question whether `Bank` should be a singleton at all

Argue both sides, in writing, in the class Javadoc:

**For:** there is conceptually one bank; a global access point avoids threading an instance through
every layer; the account id counter must not be duplicated.

**Against:**

- **Hidden dependency.** `Bank.getInstance()` inside a method means the method's signature lies —
  it depends on a bank but does not say so.
- **Global mutable state.** Anything in the process can deposit into any account.
- **Testability.** You cannot substitute a test double; you cannot run two independent scenarios in
  parallel.
- **Lifetime.** The instance lives until the JVM dies. There is no way to tear it down.
- **It is a decision about *creation* pretending to be a decision about *design*.** "There is one
  bank" is a fact about the world; "there is one `Bank` object reachable from a static field" is a
  much stronger claim.

### 6. Implement the counter-proposal too

Keep the singleton, but stop *depending* on it:

- Give `Bank` a package-private or public constructor again, used by tests and by
  `getInstance()`.
- Inject the `AccountFactory` through the constructor instead of `new`-ing it inside — so a test can
  pass a factory with a known starting id.
- Make production code accept a `Bank` as a **parameter** wherever practical, and call
  `getInstance()` only at the outermost entry point (`main`, later: the Spring configuration).

This is the *composition root* idea, and it is exactly what Spring will do for you in Exercise 01.

## Discussion questions

- Is a singleton the same thing as a static utility class? What can `Bank.getInstance()` do that a
  class full of `static` methods cannot? (Hint: interfaces, inheritance, passing it as an argument.)
- Java has a built-in singleton: a single-constant `enum`. Write `Bank` as
  `public enum Bank { INSTANCE; … }`. What does it give you for free (serialization, reflection
  safety), and why is it still rarely used for something like this?
- The singleton's instance is per **JVM**, not per application. What happens to "there is only one
  bank" when you run two instances of the service behind a load balancer?
- Preview of Sprint 12: a Spring `@Service` bean is also a singleton — one instance per application
  context. How is that *different* from `Bank.getInstance()`? Which of the four objections in task 5
  does Spring's version actually solve?

## Out of scope for this exercise

- Spring, DI containers, bean scopes — that is Sprints 6 and 12.
- Persistence, HTTP.
- Making `AccountFactory` a singleton too. (Should it be? Note your opinion, do not implement it.)

## Acceptance criteria

- [ ] `Bank` has a private constructor and a `getInstance()` returning one shared instance.
- [ ] The chosen implementation is thread-safe, and the choice is justified in a comment.
- [ ] All three implementation variants have been compared in writing.
- [ ] Tests are isolated from each other again, and the mechanism used is explicitly labelled as a
      workaround.
- [ ] `AccountFactory` is injected into `Bank`, not constructed inside it.
- [ ] The class Javadoc records the arguments **for and against** the singleton here.
- [ ] `./mvnw test` passes, and passes again when run twice in a row.

## Stretch goals (optional)

- Implement the `enum` singleton variant on a branch and compare the diff.
- Break your own singleton: use reflection (`setAccessible(true)`) to construct a second `Bank`.
  Then defend against it in the private constructor.
- Add a second "bank" concept (e.g. a test bank and a production bank) and see how far the singleton
  has to be dismantled to allow it. This is the argument for DI, demonstrated rather than asserted.
