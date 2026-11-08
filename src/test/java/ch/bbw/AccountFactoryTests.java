package ch.bbw;

import ch.bbw.accounts.Account;
import ch.bbw.accounts.PromoYouthSavingsAccount;
import ch.bbw.accounts.SalaryAccount;
import ch.bbw.accounts.SavingsAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für die Klasse AccountFactory.
 * <p>
 * Hier ist {@code instanceof} ausnahmsweise erlaubt: Getestet wird der Vertrag der Factory,
 * nämlich welcher konkrete Typ erzeugt wird.
 *
 * @author luigicavuoti
 * @version 1.0
 */
public class AccountFactoryTests {
    private final AccountFactory factory = new AccountFactory();

    @Test
    @DisplayName("createSavingsAccount returns a SavingsAccount with prefix S-")
    public void testCreateSavingsAccount() {
        Account account = factory.createSavingsAccount();

        assertInstanceOf(SavingsAccount.class, account);
        assertTrue(account.getId().startsWith("S-"));
    }

    @Test
    @DisplayName("createPromoYouthSavingsAccount returns a PromoYouthSavingsAccount with prefix Y-")
    public void testCreatePromoYouthSavingsAccount() {
        Account account = factory.createPromoYouthSavingsAccount();

        assertInstanceOf(PromoYouthSavingsAccount.class, account);
        assertTrue(account.getId().startsWith("Y-"));
    }

    @Test
    @DisplayName("createSalaryAccount returns a SalaryAccount with prefix P-")
    public void testCreateSalaryAccount() {
        Account account = factory.createSalaryAccount(-50_000);

        assertInstanceOf(SalaryAccount.class, account);
        assertTrue(account.getId().startsWith("P-"));
    }

    @Test
    @DisplayName("createSalaryAccount passes the credit limit on to the account")
    public void testCreditLimitReachesAccount() {
        Account account = factory.createSalaryAccount(-50_000);

        SalaryAccount salary = assertInstanceOf(SalaryAccount.class, account);
        assertEquals(-50_000, salary.getCreditLimit());
    }

    @Test
    @DisplayName("createSalaryAccount rejects a positive credit limit")
    public void testPositiveCreditLimitRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> factory.createSalaryAccount(50_000));
    }

    @Test
    @DisplayName("Ids should be unique over 1000 creations of mixed types")
    public void testIdsAreUnique() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            Account account = switch (i % 3) {
                case 0 -> factory.createSavingsAccount();
                case 1 -> factory.createPromoYouthSavingsAccount();
                default -> factory.createSalaryAccount(-1000);
            };

            ids.add(account.getId());
        }

        assertEquals(1000, ids.size());
    }
}
