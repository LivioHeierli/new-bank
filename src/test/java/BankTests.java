import ch.bbw.Bank;
import ch.bbw.Scheduled;
import ch.bbw.accounts.Account;
import ch.bbw.exceptions.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests für die Klasse 'Bank'.
 *
 * @author luigicavuoti
 * @version 1.0
 */
public class BankTests {
    @Test
    @DisplayName("Constructor Test, creates two SavingsAccounts")
    public void testCreate() throws Exception {
        Bank bank = new Bank();

        String first = bank.createSavingsAccount();
        String second = bank.createSavingsAccount();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
        assertEquals(first, bank.getAccount(first).getId());
        assertEquals(second, bank.getAccount(second).getId());
        assertSame(bank.getAccount(first), bank.getAccount(first));
    }

    @Test
    @DisplayName("Simple deposit on Bank-SavingsAccount should work")
    public void testDeposit() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();

        bank.deposit(id, Scheduled.now(), 1000);

        assertEquals(1000, bank.getBalance(id));
        assertEquals("Einzahlung", bank.getAccount(id).getBookings().get(0).text());
    }

    @Test
    @DisplayName("Deposit with text on Bank should store the text")
    public void testDepositWithText() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();

        bank.deposit(id, Scheduled.now(), 1000, "Lohn Oktober");

        assertEquals(1000, bank.getBalance(id));
        assertEquals("Lohn Oktober", bank.getAccount(id).getBookings().get(0).text());
    }

    @Test
    @DisplayName("Simple withdraw on Bank-SavingsAccount should work")
    public void testWithdraw() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();
        bank.deposit(id, Scheduled.now(), 1000);

        long newBalance = bank.withdraw(id, Scheduled.now(), 400);

        assertEquals(600, newBalance);
        assertEquals("Auszahlung", bank.getAccount(id).getBookings().get(1).text());
    }

    @Test
    @DisplayName("Withdraw with text on Bank should store the text")
    public void testWithdrawWithText() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();
        bank.deposit(id, Scheduled.now(), 1000);

        long newBalance = bank.withdraw(id, Scheduled.now(), 400, "Miete");

        assertEquals(600, newBalance);
        assertEquals("Miete", bank.getAccount(id).getBookings().get(1).text());
    }

    /**
     * Experimente mit print().
     */
    @Test
    @DisplayName("Not a real Test, it prints the Balance of the account")
    public void testPrint() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();
        bank.deposit(id, Scheduled.now(), 123_456, "Lohn");

        String statement = bank.getAccount(id).printStatement();

        assertTrue(statement.contains(id));
        assertTrue(statement.contains("Lohn"));
        assertTrue(statement.contains("CHF 1.23456"));
    }

    /**
     * Experimente mit print(year, month).
     */
    @Test
    @DisplayName("Not a real Test, it prints the Balance of the account")
    public void testMonthlyPrint() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSavingsAccount();
        bank.deposit(id, Scheduled.now(), 1000, "Lohn");

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Zurich"));
        LocalDate lastMonth = today.minusMonths(1);

        String thisMonth = bank.getAccount(id).printStatement(today.getYear(), today.getMonthValue());
        String otherMonth = bank.getAccount(id).printStatement(lastMonth.getYear(), lastMonth.getMonthValue());

        assertTrue(thisMonth.contains("Lohn"));
        assertFalse(otherMonth.contains("Lohn"));
    }

    /**
     * Testet den Gesamtkontostand der Bank.
     */
    @Test
    @DisplayName("Tests the balance of two SalaryAccounts from the Bank")
    public void testBalance() throws Exception {
        Bank bank = new Bank();
        String first = bank.createSalaryAccount(-1000);
        String second = bank.createSalaryAccount(-2000);

        bank.deposit(first, Scheduled.now(), 5000);
        bank.withdraw(second, Scheduled.now(), 1500);

        assertEquals(5000, bank.getBalance(first));
        assertEquals(-1500, bank.getBalance(second));
        assertEquals(3500, bank.getBalance());
    }

    /**
     * Ein überzogenes Lohnkonto zieht den Gesamtstand der Bank nach unten,
     * und die Bank kennt dafür keinen konkreten Kontotyp.
     */
    @Test
    @DisplayName("Mixed account types: an overdrawn SalaryAccount drags the bank balance down")
    public void testMixedBalance() throws Exception {
        Bank bank = new Bank();
        String savings = bank.createSavingsAccount();
        String youth = bank.createPromoYouthSavingsAccount();
        String salary = bank.createSalaryAccount(-5000);

        bank.deposit(savings, Scheduled.now(), 1000);
        bank.deposit(youth, Scheduled.now(), 10_000);   // + 100 Bonus
        bank.withdraw(salary, Scheduled.now(), 2000);

        assertEquals(1000 + 10_100 - 2000, bank.getBalance());
    }

    /**
     * Die Kreditlimite des Lohnkontos wirkt auch, wenn das Konto über die Bank erzeugt wird.
     */
    @Test
    @DisplayName("Credit limit is honoured for a SalaryAccount created through the Bank")
    public void testSalaryLimitThroughBank() throws Exception {
        Bank bank = new Bank();
        String id = bank.createSalaryAccount(-500);

        assertEquals(-500, bank.withdraw(id, Scheduled.now(), 500));
        assertThrows(InvalidAmountException.class,
                () -> bank.withdraw(id, Scheduled.now(), 1));
        assertEquals(1, bank.getAccount(id).getBookings().size());
    }

    /**
     * Ein positives Limit wird schon beim Erzeugen abgelehnt, und es wird kein Konto registriert.
     */
    @Test
    @DisplayName("A positive credit limit is rejected by the Bank")
    public void testPositiveLimitRejected() {
        Bank bank = new Bank();

        assertThrows(IllegalArgumentException.class, () -> bank.createSalaryAccount(1));
        assertEquals(0, bank.getBalance());
        assertTrue(bank.top5HighestBalances().isEmpty());
    }

    /**
     * Tested die Ausgabe der "top 5" konten.
     */
    @Test
    @DisplayName("Tests the List of the top 5 balances")
    public void testTop5() throws Exception {
        Bank bank = new Bank();
        MixedBank mixed = MixedBank.fill(bank);

        List<String> ids = bank.top5HighestBalances().stream().map(Account::getId).toList();

        assertEquals(List.of(mixed.youth, mixed.s1, mixed.p2, mixed.s2, mixed.s3), ids);
    }

    /**
     * Tested die Ausgabe der "top 5" konten.
     */
    @Test
    @DisplayName("Tests the List of the bottom 5 balances")
    public void testBottom5() throws Exception {
        Bank bank = new Bank();
        MixedBank mixed = MixedBank.fill(bank);

        List<String> ids = bank.top5LowestBalances().stream().map(Account::getId).toList();

        // p1 ist überzogen und steht deshalb zuoberst in der Liste der tiefsten Konten.
        assertEquals(List.of(mixed.p1, mixed.s4, mixed.s3, mixed.s2, mixed.p2), ids);
    }

    /**
     * Sieben Konten aller drei Typen mit den Salden
     * s1 = 1000, s2 = 500, s3 = 300, s4 = 100, youth = 10_100 (inkl. Bonus), p1 = -2000, p2 = 700.
     */
    private static class MixedBank {
        String s1, s2, s3, s4, youth, p1, p2;

        static MixedBank fill(Bank bank) throws Exception {
            MixedBank m = new MixedBank();

            m.s1 = bank.createSavingsAccount();
            m.s2 = bank.createSavingsAccount();
            m.s3 = bank.createSavingsAccount();
            m.s4 = bank.createSavingsAccount();
            m.youth = bank.createPromoYouthSavingsAccount();
            m.p1 = bank.createSalaryAccount(-5000);
            m.p2 = bank.createSalaryAccount(-5000);

            bank.deposit(m.s1, Scheduled.now(), 1000);
            bank.deposit(m.s2, Scheduled.now(), 500);
            bank.deposit(m.s3, Scheduled.now(), 300);
            bank.deposit(m.s4, Scheduled.now(), 100);
            bank.deposit(m.youth, Scheduled.now(), 10_000);
            bank.withdraw(m.p1, Scheduled.now(), 2000);
            bank.deposit(m.p2, Scheduled.now(), 700);

            return m;
        }
    }

}