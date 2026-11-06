import ch.bbw.Bank;
import ch.bbw.Scheduled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Tests für die Klasse 'Bank'.
 *
 * @author luigicavuoti
 * @version 1.0
 */
public class BankTests {
    @Test
    @DisplayName("Constructor Test, creates two SavingsAccounts")
    public void testCreate() {
        fail("ToDo");
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
    public void testPrint() {
        fail("ToDo");
    }

    /**
     * Experimente mit print(year, month).
     */
    @Test
    @DisplayName("Not a real Test, it prints the Balance of the account")
    public void testMonthlyPrint() {
        fail("ToDo");
    }

    /**
     * Testet den Gesamtkontostand der Bank.
     */
    @Test
    @DisplayName("Tests the balance of two SalaryAccounts from the Bank")
    public void testBalance() {
        fail("ToDo");
    }

    /**
     * Tested die Ausgabe der "top 5" konten.
     */
    @Test
    @DisplayName("Tests the List of the top 5 balances")
    public void testTop5() {
        fail("ToDo");
    }

    /**
     * Tested die Ausgabe der "top 5" konten.
     */
    @Test
    @DisplayName("Tests the List of the bottom 5 balances")
    public void testBottom5() {
        fail("ToDo");
    }

}