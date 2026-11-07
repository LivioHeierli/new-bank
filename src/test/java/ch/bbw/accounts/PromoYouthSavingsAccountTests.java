package ch.bbw.accounts;

import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import ch.bbw.exceptions.InvalidDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests für die Klasse PromoYouthSavingsAccount.
 *
 * @author luigicavuoti
 * @version 1.0
 */
public class PromoYouthSavingsAccountTests {
    @Test
    @DisplayName("A deposit should be rewarded with 1% bonus")
    public void testBonus() throws Exception {
        Account account = new PromoYouthSavingsAccount("Y-1");

        long newBalance = account.deposit(Scheduled.now(), 10_000);

        assertEquals(10_100, newBalance);
        assertEquals(2, account.getBookings().size());
        assertEquals(10_000, account.getBookings().get(0).amount());
        assertEquals(100, account.getBookings().get(1).amount());
        assertEquals("Bonus 1%", account.getBookings().get(1).text());
    }

    @Test
    @DisplayName("Bonus boundary: 100 Millirappen get a bonus, 99 do not")
    public void testBonusBoundary() throws Exception {
        Account withBonus = new PromoYouthSavingsAccount("Y-1");
        Account withoutBonus = new PromoYouthSavingsAccount("Y-2");

        withBonus.deposit(Scheduled.now(), 100);
        withoutBonus.deposit(Scheduled.now(), 99);

        assertEquals(101, withBonus.getBalance());
        assertEquals(2, withBonus.getBookings().size());
        assertEquals(99, withoutBonus.getBalance());
        assertEquals(1, withoutBonus.getBookings().size());
    }

    @Test
    @DisplayName("A rejected deposit should not book any bonus")
    public void testRejectedDeposit() {
        Account account = new PromoYouthSavingsAccount("Y-1");

        assertThrows(InvalidAmountException.class,
                () -> account.deposit(Scheduled.now(), 0));
        assertThrows(InvalidDateException.class,
                () -> account.deposit(Scheduled.now().minusDays(1), 10_000));

        assertEquals(0, account.getBookings().size());
        assertEquals(0, account.getBalance());
    }

    @Test
    @DisplayName("The account must not be overdrawn")
    public void testNoOverdraft() throws Exception {
        Account account = new PromoYouthSavingsAccount("Y-1");
        account.deposit(Scheduled.now(), 10_000);

        assertEquals(0, account.withdraw(Scheduled.now(), 10_100));

        assertThrows(InvalidAmountException.class,
                () -> account.withdraw(Scheduled.now(), 1));
    }
}