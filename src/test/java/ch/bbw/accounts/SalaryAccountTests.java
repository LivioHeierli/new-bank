package ch.bbw.accounts;

import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für die Klasse SalaryAccount.
 *
 * @author luigicavuoti
 * @version 2.0
 */
public class SalaryAccountTests {
	@Test
	@DisplayName("Credit limit is stored and can be read")
	public void testCreditLimit() {
		SalaryAccount account = new SalaryAccount("P-1", -1000);

		assertEquals(-1000, account.getCreditLimit());
		assertEquals(0, account.getBalance());
	}

	@Test
	@DisplayName("A positive credit limit should be rejected")
	public void testPositiveLimitRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> new SalaryAccount("P-1", 1));
	}

	@Test
	@DisplayName("Withdrawing down to exactly the credit limit should work")
	public void testWithdrawToLimit() throws Exception {
		Account account = new SalaryAccount("P-1", -1000);

		long newBalance = account.withdraw(Scheduled.now(), 1000);

		assertEquals(-1000, newBalance);
		assertTrue(account.getBalance() < 0);
	}

	@Test
	@DisplayName("Withdrawing one Millirappen below the credit limit should be rejected")
	public void testWithdrawBelowLimit() throws Exception {
		Account account = new SalaryAccount("P-1", -1000);
		account.deposit(Scheduled.now(), 500);

		assertThrows(InvalidAmountException.class,
				() -> account.withdraw(Scheduled.now(), 1501));

		assertEquals(1, account.getBookings().size());
		assertEquals(500, account.getBalance());
	}

	@Test
	@DisplayName("With credit limit 0 the account behaves like a savings account")
	public void testLimitZero() throws Exception {
		Account account = new SalaryAccount("P-1", 0);
		account.deposit(Scheduled.now(), 1000);

		assertEquals(0, account.withdraw(Scheduled.now(), 1000));
		assertThrows(InvalidAmountException.class,
				() -> account.withdraw(Scheduled.now(), 1));
	}

	@Test
	@DisplayName("Different account types in one list should be summed polymorphically")
	public void testPolymorphism() throws Exception {
		Account savings = new SavingsAccount("S-1");
		Account salary = new SalaryAccount("P-1", -1000);
		Account youth = new PromoYouthSavingsAccount("Y-1");

		savings.deposit(Scheduled.now(), 1000);
		salary.withdraw(Scheduled.now(), 500);
		youth.deposit(Scheduled.now(), 10_000);

		List<Account> accounts = List.of(savings, salary, youth);
		long total = accounts.stream().mapToLong(Account::getBalance).sum();

		// 1000 + (-500) + (10_000 + 100 Bonus)
		assertEquals(10_600, total);
	}
}