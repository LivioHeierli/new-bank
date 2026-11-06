# Exercise 02 — Bookings

> **Sprint 2** of `start.md`. Numbering note: `01-Exercise-build-the-be.md` covers the Spring Boot
> extension (Sprints 6–11) and comes *after* this exercise and 03–05.

## Goal

Make `Booking` a first-class part of the domain: give a booking a **text**, expose the booking
history on an `Account`, and **derive the balance from the bookings** instead of keeping it as a
separate field.

This is the first exercise where you change an existing design rather than add to an empty project.
The point is to feel the difference between *storing* a value and *deriving* it.

## Prerequisites / current state

Read these files before you start:

- `src/main/java/ch/bbw/Booking.java`
- `src/main/java/ch/bbw/accounts/Account.java`
- `src/main/java/ch/bbw/Scheduled.java`

### What already exists

- `Booking` is a Java **record** with two components: `date` (long) and `amount` (long, in
  Millirappen).
- `Account` already keeps a `List<Booking> bookings` and appends to it on every `deposit()` and
  `withdraw()`. A withdrawal is stored as a booking with a **negative** amount.
- `Account` also keeps a separate `private long balance` field, which it increments/decrements in
  parallel with the booking list.

### What is missing / wrong

1. **No `text`.** A booking has no description, so a statement cannot say *why* money moved.
2. **The booking list is write-only.** `Account` has no getter for `bookings` — nothing outside the
   class can ever read the history that is being so carefully maintained.
3. **Balance is duplicated state.** `balance` and `bookings` both encode the same information. Two
   sources of truth for one fact is a bug waiting to happen: any future code path that adds a
   booking without touching `balance` (or vice versa) silently corrupts the account.
4. **The date semantics are inconsistent.** `Booking`'s Javadoc says *"Banktage seit 1.1.1970"*
   (bank days since 1970), but `Account` actually stores `date.toUnix()`, which is **epoch
   milliseconds**. One of the two has to give.

## Work test-first (TDD)

Task 1 below *is* the test suite for this exercise — it describes the target API (`Booking(date,
amount, text)`, `Account.getBookings()`, a derived `getBalance()`) before any of it exists in the
code. Write those tests first:

1. Write (or extend) a test for one piece of behaviour from the list in Task 1.
2. Run `./mvnw test` and confirm it fails — for the right reason (assertion failure, or the code
   doesn't compile yet because the API doesn't exist). This is the **red** step; don't skip it, it's
   how you know the test actually tests something.
3. Implement just enough of Tasks 2–7 to make that test pass (**green**).
4. Refactor if needed, keeping all tests green, then move to the next piece of behaviour.

Do not write `Booking`'s new constructor, `getBookings()`, or the derived `getBalance()` before a
failing test demands them.

## Tasks

### 1. Tests

Implement the stubs that currently just call `fail("ToDo")`:

- `src/test/java/BookingTests.java` — `testInitialization()`, `testPrint()`.
- `src/test/java/ch/bbw/accounts/AccountTests.java` — `testDeposit()`, `testWithdraw()`,
  `testPrint()`, `testMonthlyPrint()`.

At minimum cover:

- a new account has an empty booking list and a balance of `0`;
- a deposit appends exactly one booking with a **positive** amount and the given text;
- a withdrawal appends exactly one booking with a **negative** amount;
- the balance after *n* operations equals the sum of the booking amounts;
- `getBookings()` cannot be used to modify the account (expect
  `UnsupportedOperationException`);
- a rejected operation (amount `<= 0`, past-due date) adds **no** booking and leaves the balance
  unchanged.

That last one matters: make sure the exception is thrown *before* the booking is appended.

Yes, most of these will fail to even compile at first — `Booking` doesn't take a `text` yet and
`getBookings()` doesn't exist. That's the point: the test names the API you're about to build.

### 2. Add a text to `Booking`

Extend the record to `Booking(long date, long amount, String text)`.

While you are in the file: the record currently declares an empty compact constructor and manually
overrides `date()` and `amount()` with bodies that just return the field. A record generates both
for you. Delete the redundant code and keep the Javadoc.

Decide what happens when `text` is `null` or blank — reject it in the compact constructor, or
default it to something like `"Einzahlung"` / `"Auszahlung"`. Write down your choice.

### 3. Thread the text through `Account` and `Bank`

`Account.deposit(...)` / `withdraw(...)` and the corresponding `Bank` methods need to accept a text
and pass it on. Keep the existing two-argument signatures as convenience overloads that supply a
default text, so you do not have to touch every call site at once.

### 4. Expose the booking history

Add `List<Booking> getBookings()` to `Account`.

Return an **unmodifiable view** (`List.copyOf(...)` or `Collections.unmodifiableList(...)`), not the
internal list. If you hand out the real list, any caller can add a booking behind the account's back
— which is exactly the invariant you are about to establish in task 5.

### 5. Derive the balance from the bookings

Remove the `balance` field. Reimplement:

```java
public long getBalance() {
    return bookings.stream()
                   .mapToLong(Booking::amount)
                   .sum();
}
```

Now there is exactly one source of truth. `deposit()` and `withdraw()` still need to return the new
balance — have them append the booking and then return `getBalance()`.

Watch out for `SavingsAccount.withdraw(...)`: it calls `getBalance()` *before* delegating to
`super.withdraw(...)`. Verify that overdraft protection still behaves correctly after the change.

### 6. Fix the date semantics

Pick one and make code and Javadoc agree:

- **Option A (recommended):** the booking date is a **Unix timestamp in milliseconds**. Update
  `Booking`'s Javadoc to say so.
- **Option B:** the booking date really is *bank days since 1.1.1970*. Then `Account` must convert
  (`toUnix() / 86_400_000`) before constructing the `Booking`, and you lose the time of day.

### 7. Print a statement

Add a method that renders the booking history as a readable account statement — for example
`String printStatement()` on `Account`, one line per booking with date, amount and text, plus the
closing balance.

Format amounts for humans: the domain stores **Millirappen**, so `123_456` is `CHF 1.23456`. Decide
where that formatting belongs (hint: not inside `Booking`).

## Discussion questions

- Deriving the balance is now O(n) in the number of bookings. When would that become a problem, and
  what would you do about it? (Keywords: caching, snapshot balance, event sourcing.)
- Why is `Booking` a `record` and not a normal class? What would break if bookings were mutable?
- `Account.bookings` is `final`, but the list it points at is not. What does `final` actually
  guarantee here?

## Out of scope for this exercise

- New account types — that is Exercise 03.
- Changing `AccountFactory` — that is Exercise 04.
- Anything HTTP or persistence related.

## Acceptance criteria

- [x] `Booking` is a record with `date`, `amount` and `text`, and no redundant hand-written accessors.
- [x] `Account` has no `balance` field; `getBalance()` is computed from the bookings.
- [x] `getBookings()` returns a read-only view of the history.
- [x] Booking date semantics are consistent between code and Javadoc.
- [x] A statement can be printed with readable amounts.
- [x] `./mvnw test` passes; the `BookingTests` and `AccountTests` stubs are implemented, not deleted.

## Stretch goals (optional)

- `printStatement(int year, int month)` — filter the history to one month (see the
  `testMonthlyPrint()` stub).
- Give `Booking` a `type` (`DEPOSIT` / `WITHDRAWAL` / `FEE` / `INTEREST`) as an enum instead of
  inferring it from the sign of `amount`. Which of the two designs do you prefer, and why?
- Add a booking id or running number so two identical bookings on the same day can be told apart.
