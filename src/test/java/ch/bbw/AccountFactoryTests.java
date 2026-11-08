package ch.bbw;

import ch.bbw.accounts.Account;
import ch.bbw.accounts.PromoYouthSavingsAccount;
import ch.bbw.accounts.SalaryAccount;
import ch.bbw.accounts.SavingsAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    @DisplayName("create(type, limit) dispatches to the right subtype")
    public void testCreateDispatchesByType() {
        assertInstanceOf(SavingsAccount.class, factory.create(AccountType.SAVINGS, 0));
        assertInstanceOf(PromoYouthSavingsAccount.class, factory.create(AccountType.PROMO_YOUTH, 0));

        SalaryAccount salary = assertInstanceOf(SalaryAccount.class,
                factory.create(AccountType.SALARY, -2_000));
        assertEquals(-2_000, salary.getCreditLimit());
    }

    @Test
    @DisplayName("Every AccountType produces an id with its own prefix")
    public void testEveryTypeHasItsPrefix() {
        for (AccountType type : AccountType.values()) {
            Account account = factory.create(type, 0);

            assertTrue(account.getId().startsWith(type.prefix() + "-"),
                    type + " -> " + account.getId());
        }
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

    @Test
    @DisplayName("Ids should stay unique when several threads create accounts at the same time")
    public void testIdsAreUniqueAcrossThreads() throws Exception {
        int threads = 8;
        int perThread = 500;
        Set<String> ids = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                Callable<Void> task = () -> {
                    for (int i = 0; i < perThread; i++) {
                        ids.add(factory.createSavingsAccount().getId());
                    }
                    return null;
                };
                futures.add(pool.submit(task));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdown();
        }

        assertEquals(threads * perThread, ids.size());
    }
}
