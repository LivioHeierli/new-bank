package ch.bbw.accounts;


import ch.bbw.Booking;
import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import ch.bbw.exceptions.InvalidDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests für die Klasse Account.
 *
 * @author luigicavuoti
 * @version 2.1
 */
public class AccountTests {
    /**
     * Tested die Initialisierung eines Kontos.
     */
    @Test
    @DisplayName("Simple constructor-Test for SavingsAccount should work")
    public void testInit() {
        Account account = new SavingsAccount("S-1");

        assertTrue(account.getBookings().isEmpty());
        assertEquals(0, account.getBalance());
    }

    /**
     * Die Buchungsliste darf von aussen nicht veränderbar sein.
     */
    @Test
    @DisplayName("getBookings() should return a read-only list")
    public void testBookingsReadOnly() {
        Account account = new SavingsAccount("S-1");

        assertThrows(UnsupportedOperationException.class,
                () -> account.getBookings().add(new Booking(0, 100, "Test")));
    }

    /**
     * Testet das Einzahlen auf ein Konto.
     */
    @Test
    @DisplayName("Simple deposit on SavingsAccount should work")
    public void testDeposit() throws Exception {
        Account account = new SavingsAccount("S-1");

        long newBalance = account.deposit(Scheduled.now(), 1000);

        assertEquals(1, account.getBookings().size());
        assertTrue(account.getBookings().get(0).amount() > 0);
        assertEquals(1000, account.getBalance());
        assertEquals(1000, newBalance);
    }

    /**
     * Testet das Einzahlen mit eigenem Text.
     */
    @Test
    @DisplayName("Deposit with text should store the text in the booking")
    public void testDepositWithText() throws Exception {
        Account account = new SavingsAccount("S-1");

        account.deposit(Scheduled.now(), 1000, "Lohn Oktober");

        assertEquals(1, account.getBookings().size());
        assertEquals("Lohn Oktober", account.getBookings().get(0).text());
        assertEquals(1000, account.getBookings().get(0).amount());
    }

    /**
     * Testet das Abheben von einem Konto.
     */
    @Test
    @DisplayName("Simple withdraw from SavingsAccount should work")
    public void testWithdraw() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 1000);

        long newBalance = account.withdraw(Scheduled.now(), 400);

        assertEquals(2, account.getBookings().size());
        assertTrue(account.getBookings().get(1).amount() < 0);
        assertEquals(600, account.getBalance());
        assertEquals(600, newBalance);
    }

    /**
     * Testet das Abheben mit eigenem Text.
     */
    @Test
    @DisplayName("Withdraw with text should store the text in the booking")
    public void testWithdrawWithText() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 1000);

        account.withdraw(Scheduled.now(), 400, "Miete");

        assertEquals(2, account.getBookings().size());
        assertEquals("Miete", account.getBookings().get(1).text());
        assertEquals(-400, account.getBookings().get(1).amount());
    }

    /**
     * Das Sparkonto darf auch bei einer Abhebung mit Text nicht überzogen werden.
     */
    @Test
    @DisplayName("SavingsAccount must not be overdrawn, also not with text")
    public void testSavingsOverdraftWithText() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 100);

        assertThrows(InvalidAmountException.class,
                () -> account.withdraw(Scheduled.now(), 200, "Zu viel"));

        assertEquals(1, account.getBookings().size());
        assertEquals(100, account.getBalance());
    }

    /**
     * Der Saldo ist die Summe aller Buchungsbeträge.
     */
    @Test
    @DisplayName("Balance should equal the sum of all booking amounts")
    public void testBalanceIsSumOfBookings() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 1000);
        account.deposit(Scheduled.now(), 500, "Geschenk");
        account.withdraw(Scheduled.now(), 300);

        long sum = account.getBookings().stream()
                .mapToLong(Booking::amount)
                .sum();

        assertEquals(1200, sum);
        assertEquals(sum, account.getBalance());
    }

    /**
     * Das Buchungsdatum ist ein Unix-Zeitstempel in Millisekunden.
     */
    @Test
    @DisplayName("Booking date should be a Unix timestamp in milliseconds")
    public void testBookingDateIsEpochMillis() throws Exception {
        Account account = new SavingsAccount("S-1");

        long before = System.currentTimeMillis();
        account.deposit(Scheduled.now(), 1000);
        long after = System.currentTimeMillis();

        long date = account.getBookings().get(0).date();

        assertTrue(date >= before && date <= after);
    }

    /**
     * Abgelehnte Operationen dürfen keine Buchung anlegen und den Saldo nicht ändern.
     */
    @Test
    @DisplayName("Rejected operations should not change bookings or balance")
    public void testRejectedOperations() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 1000);

        assertThrows(InvalidAmountException.class,
                () -> account.deposit(Scheduled.now(), 0));
        assertThrows(InvalidDateException.class,
                () -> account.deposit(Scheduled.now().minusDays(1), 100));
        assertThrows(InvalidAmountException.class,
                () -> account.withdraw(Scheduled.now(), -5));
        assertThrows(InvalidDateException.class,
                () -> account.withdraw(Scheduled.now().minusDays(1), 100));

        assertEquals(1, account.getBookings().size());
        assertEquals(1000, account.getBalance());
    }

    @Test
    @DisplayName("TreeMap of two Account-References should work")
    public void testReferences() {
        fail("ToDo");
    }

    @Test
    @DisplayName("TreeMap of two Account-References should work")
    public void testCanTransact() {
        fail("ToDo");
    }

    /**
     * Der Kontoauszug zeigt jede Buchung mit lesbarem Betrag und den Saldo.
     */
    @Test
    @DisplayName("printStatement should list bookings with readable amounts and the balance")
    public void testPrint() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 123_456, "Lohn");
        account.withdraw(Scheduled.now(), 23_456, "Miete");

        String statement = account.printStatement();

        assertTrue(statement.contains("Lohn"));
        assertTrue(statement.contains("CHF 1.23456"));
        assertTrue(statement.contains("Miete"));
        assertTrue(statement.contains("-CHF 0.23456"));
        assertTrue(statement.contains("Saldo"));
        assertTrue(statement.contains("CHF 1.00000"));
    }

    /**
     * Der Monatsauszug zeigt nur die Buchungen des gewünschten Monats.
     */
    @Test
    @DisplayName("printStatement(year, month) should only list bookings of that month")
    public void testMonthlyPrint() throws Exception {
        Account account = new SavingsAccount("S-1");
        account.deposit(Scheduled.now(), 1000, "Lohn");

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Zurich"));
        LocalDate lastMonth = today.minusMonths(1);

        String thisMonth = account.printStatement(today.getYear(), today.getMonthValue());
        String otherMonth = account.printStatement(lastMonth.getYear(), lastMonth.getMonthValue());

        assertTrue(thisMonth.contains("Lohn"));
        assertFalse(otherMonth.contains("Lohn"));
    }

}