package ch.bbw;


import ch.bbw.accounts.Account;
import ch.bbw.exceptions.*;


import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Die Bank.
 *
 * @author luigicavuoti, lro@gmx.ch
 * @version 2.1
 */
public class Bank {
    /**
     * Liste aller Konti.
     */
    private final TreeMap<String, Account> accounts;
    private final AccountFactory accountFactory;
    /**
     * Initialisiert eine neue Bank.
     */
    public Bank() {
        this.accounts = new TreeMap<String, Account>();
        accountFactory = new AccountFactory();
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