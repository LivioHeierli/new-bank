package ch.bbw.accounts;

import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import ch.bbw.exceptions.InvalidDateException;

/**
 * Lohnkonto. Das Konto darf bis zur Kreditlimite überzogen werden.
 *
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 1.0
 */
public class SalaryAccount extends Account {
    /**
     * Die Kreditlimite in Millirappen. Negativ oder 0: Das tiefste Saldo,
     * das das Konto erreichen darf.
     */
    private final long creditLimit;

    /**
     * Initialisiert ein Lohnkonto.
     *
     * @param id          die Kontonummer
     * @param creditLimit die Kreditlimite (negative Zahl oder 0)
     * @throws IllegalArgumentException falls die Kreditlimite positiv ist
     */
    public SalaryAccount(String id, long creditLimit) {
        super(id);

        if (creditLimit > 0)
            throw new IllegalArgumentException("creditLimit must be negative or zero");

        this.creditLimit = creditLimit;
    }

    /**
     * Gibt die Kreditlimite zurück.
     *
     * @return die Kreditlimite in Millirappen (negativ oder 0)
     */
    public long getCreditLimit() {
        return creditLimit;
    }

    /**
     * Hebt den gegebenen Betrag vom Konto ab. Das Saldo darf
     * die Kreditlimite nicht unterschreiten.
     *
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @param text   der Buchungstext
     * @return das neue Saldo
     */
    @Override
    public long withdraw(Scheduled date, long amount, String text) throws InvalidAmountException, InvalidDateException {
        if (getBalance() - amount < creditLimit)
            throw new InvalidAmountException("Credit limit of " + creditLimit + " would be exceeded");

        return super.withdraw(date, amount, text);
    }

    /**
     * Gibt die Bezeichnung des Kontotyps zurück.
     *
     * @return "Lohnkonto"
     */
    @Override
    public String getAccountType() {
        return "Lohnkonto";
    }
}