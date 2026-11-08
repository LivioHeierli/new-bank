package ch.bbw;


import ch.bbw.accounts.Account;
import ch.bbw.exceptions.*;


import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The bank and its account registry.
 *
 * <p>The application-wide bank is exposed through a lazy, thread-safe
 * initialization-on-demand holder. Eager initialization is also thread-safe,
 * but creates the instance even when it is never used. A synchronized lazy
 * accessor is thread-safe too, but pays for synchronization on every call.
 * The holder idiom is lazy and relies on the JVM's class-initialization
 * guarantees, so synchronization is needed only during first initialization.
 * Double-checked locking is unsafe without {@code volatile}: the JVM may
 * publish the reference before the new object's fields have been initialized.
 * </p>
 *
 * <p>A singleton is defensible because the application models one bank, gives
 * callers a global access point, and avoids duplicating the account factory's
 * id sequence. It also introduces a hidden dependency, globally mutable state,
 * makes test doubles and parallel scenarios harder, and ties the instance's
 * lifetime to the JVM. One bank object per JVM also does not imply one bank
 * across multiple service processes.</p>
 *
 * <p>Prefer passing a {@code Bank} to collaborators where practical and
 * obtaining the singleton only at the composition root. The injectable
 * constructor supports that approach and isolated tests. The package-private
 * reset method exists only to clean up singleton state in tests; needing it is
 * a design smell, not a production lifecycle solution. Singleton creation is
 * thread-safe, but the mutable account registry itself is not.</p>
 *
 * @author luigicavuoti, lro@gmx.ch
 * @version 2.1
 */
public class Bank {
    private static class Holder {
        private static final Bank INSTANCE = new Bank(new AccountFactory());
    }

    /**
     * Liste aller Konti.
     */
    private final TreeMap<String, Account> accounts;
    private final AccountFactory accountFactory;

    Bank(AccountFactory accountFactory) {
        this.accounts = new TreeMap<String, Account>();
        this.accountFactory = Objects.requireNonNull(accountFactory, "accountFactory");
    }

    /**
     * Returns the application-wide bank instance.
     *
     * @return the shared bank
     */
    public static Bank getInstance() {
        return Holder.INSTANCE;
    }

    static void resetForTesting() {
        getInstance().accounts.clear();
    }

    /**
     * Erzeugt ein neues Sparkonto
     *
     * @return die neue Kontonummer
     */
    public String createSavingsAccount() {
        var account = accountFactory.createSavingsAccount();
        var id = account.getId();

        accounts.put(id, account);

        return id;
    }

    /**
     * Erzeugt ein neues Promo-Jugendsparkonto
     *
     * @return die neue Kontonummer
     */
    public String createPromoYouthSavingsAccount() {
        var account = accountFactory.createPromoYouthSavingsAccount();
        var id = account.getId();

        accounts.put(id, account);

        return id;
    }

    /**
     * Erzeugt ein neues Lohnkonto
     *
     * @param creditLimit Kreditlimite (negative Zahl)
     * @return String die neue Kontonummer
     */
    public String createSalaryAccount(long creditLimit) throws IllegalArgumentException {
        var account = accountFactory.createSalaryAccount(creditLimit);
        var id = account.getId();

        accounts.put(id, account);

        return id;

    }

    /**
     * Gibt den Kontostand der Bank zurück.
     *
     * @return long der Kontostand der Bank
     */
    public long getBalance() {
        return accounts
                .values()
                .stream()
                .mapToLong(Account::getBalance)
                .sum();
    }

    /**
     * Gibt den Kontostand des Kontos mit der gegebenen
     * Kontonummer zurück.
     *
     * <p>
     * Falls kein Konto mit der gesuchten Kontonummer
     * existiert, gibt diese Methode 0 (zero) zurück.
     * </p>
     *
     * @param id die Kontonummer
     * @return long der Kontostand des Kontos
     */
    public long getBalance(String id) throws InvalidAmountException {
        return getAccount(id).getBalance();
    }

    /**
     * Zahlt den gegebenen Betrag mit dem Standardtext "Einzahlung"
     * auf das Konto mit der gegebenen Kontonummer ein.
     *
     * @param id     die Kontonummer
     * @param date   das Transaktionsdatum
     * @param amount der einzuzahlende Betrag
     */
    public void deposit(String id, Scheduled date,
                        long amount) throws InvalidAmountException, InvalidDateException {
        deposit(id, date, amount, "Einzahlung");
    }

    /**
     * Zahlt den gegebenen Betrag auf das Konto mit
     * der gegebenen Kontonummer ein.
     *
     * <p>
     * Diese Methode wirft eine Exception, falls das Konto
     * nicht existiert, oder falls die Einzahlung auf dem
     * Konto nicht funktioniert.
     * </p>
     *
     * @param id     die Kontonummer
     * @param date   das Transaktionsdatum
     * @param amount der einzuzahlende Betrag
     * @param text   der Buchungstext
     */
    public void deposit(String id, Scheduled date,
                        long amount, String text) throws InvalidAmountException, InvalidDateException {
        var account = getAccount(id);

        account.deposit(date, amount, text);
    }

    /**
     * Hebt den gegebenen Betrag mit dem Standardtext "Auszahlung"
     * vom Konto mit der gegebenen Kontonummer ab.
     *
     * @param id     die Kontonummer
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @return new balance
     */
    public long withdraw(String id, Scheduled date,
                         long amount) throws InvalidAmountException, InvalidDateException {
        return withdraw(id, date, amount, "Auszahlung");
    }

    /**
     * Hebt den gegebenen Betrag vom Konto mit
     * der gegebenen Kontonummer ab.
     *
     * <p>
     * Diese Methode wirft eine Exception, falls das Konto
     * nicht existiert, oder falls das Abheben vom Konto
     * nicht funktioniert.
     * </p>
     *
     * @param id     die Kontonummer
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @param text   der Buchungstext
     * @return new balance
     */
    public long withdraw(String id, Scheduled date,
                         long amount, String text) throws InvalidAmountException, InvalidDateException {
        var account = getAccount(id);

        return account.withdraw(date, amount, text);
    }

    public List<Account> top5HighestBalances() {
        return accounts.values()
                .stream()
                .sorted(Comparator.comparingLong(Account::getBalance).reversed())
                .limit(5)
                .toList();
    }

    public List<Account> top5LowestBalances() {
        return accounts.values()
                .stream()
                .sorted(Comparator.comparingLong(Account::getBalance))
                .limit(5)
                .toList();
    }

    /**
     * Getter of the property <tt>account</tt>
     *
     * @return Returns the account.
     */
    public Account getAccount(String id) throws InvalidAmountException {

        var account = Optional.ofNullable(accounts.get(id));

        if (account.isEmpty()) {
            throw new InvalidAmountException("Invalid bank id");
        }

        return account.get();
    }
}