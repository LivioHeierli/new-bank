package ch.bbw.accounts;

import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests für die Klasse SavingsAccount.
 *
 * @author luigicavuoti
 * @version 2.0
 */
public class SavingsAccountTests {
	@Test
	@DisplayName("Withdrawing exactly the balance should work")
	public void testWithdrawExactBalance() throws Exception {
		Account account = new SavingsAccount("S-1");
		account.deposit(Scheduled.now(), 1000);

		long newBalance = account.withdraw(Scheduled.now(), 1000);

		assertEquals(0, newBalance);
		assertEquals(0, account.getBalance());
	}

	@Test
	@DisplayName("Withdrawing one Millirappen more than the balance should be rejected")
	public void testWithdrawTooMuch() throws Exception {
		Account account = new SavingsAccount("S-1");
		account.deposit(Scheduled.now(), 1000);

		assertThrows(InvalidAmountException.class,
				() -> account.withdraw(Scheduled.now(), 1001));

		assertEquals(1, account.getBookings().size());
		assertEquals(1000, account.getBalance());
	}
}