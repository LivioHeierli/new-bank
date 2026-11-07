package ch.bbw.accounts;

import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import ch.bbw.exceptions.InvalidDateException;

/**
 * Jugendsparkonto. Jede Einzahlung wird mit 1% Bonus belohnt.
 * <p>
 * Entscheid zur Oberklasse: Das Konto erbt direkt von {@link Account} (wie
 * Lohn- und Sparkonto) und ist damit unabhängig von {@link SavingsAccount}.
 * Dafür prüft es die Regel "kein Überzug" selbst.
 *
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 1.0
 */
public class PromoYouthSavingsAccount extends Account {
    /**
     * Bonus pro Einzahlung: 1 Prozent, also ein Hundertstel.
     */
    private static final long BONUS_DIVISOR = 100;

    /**
     * Initialisiert ein Jugendsparkonto.
     *
     * @param id die Kontonummer
     */
    public PromoYouthSavingsAccount(String id) {
        super(id);
    }

    /**
     * Zahlt den Betrag ein und bucht zusätzlich 1% Bonus.
     * Ist der Bonus kleiner als 1 Millirappen, wird keine Bonusbuchung angelegt.
     *
     * @param date   das Transaktionsdatum
     * @param amount der einzuzahlende Betrag
     * @param text   der Buchungstext
     * @return das neue Saldo
     */
    @Override
    public long deposit(Scheduled date, long amount, String text) throws InvalidAmountException, InvalidDateException {
        super.deposit(date, amount, text);

        long bonus = amount / BONUS_DIVISOR;

        if (bonus > 0)
            super.deposit(date, bonus, "Bonus 1%");

        return getBalance();
    }

    /**
     * Hebt den gegebenen Betrag vom Konto ab. Das Konto darf nicht überzogen werden.
     *
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @param text   der Buchungstext
     * @return das neue Saldo
     */
    @Override
    public long withdraw(Scheduled date, long amount, String text) throws InvalidAmountException, InvalidDateException {
        if (getBalance() < amount)
            throw new InvalidAmountException("Amount is greater than balance");

        return super.withdraw(date, amount, text);
    }

    /**
     * Gibt die Bezeichnung des Kontotyps zurück.
     *
     * @return "Jugendsparkonto"
     */
    @Override
    public String getAccountType() {
        return "Jugendsparkonto";
    }
}