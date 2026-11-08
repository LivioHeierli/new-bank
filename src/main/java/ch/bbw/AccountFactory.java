package ch.bbw;

import ch.bbw.accounts.*;

/**
 * Die Factory ist der einzige Ort, der entscheidet, welcher konkrete
 * {@link Account}-Typ erzeugt wird und welche Kontonummer er bekommt.
 *
 * <p>
 * Entscheid zur API: {@link Bank} verwendet die drei benannten Methoden
 * ({@link #createSavingsAccount()}, {@link #createPromoYouthSavingsAccount()},
 * {@link #createSalaryAccount(long)}). Sie sind selbsterklärend und zwingen
 * niemanden, für ein Sparkonto eine sinnlose Kreditlimite anzugeben.
 * {@link #create(AccountType, long)} ist die einzige Stelle mit der
 * Typ-Entscheidung; ein neuer Kontotyp braucht dort nur einen neuen
 * {@code case}.
 * </p>
 *
 * <p>
 * Das Argument {@code creditLimit} von {@code create(...)} ist nur für
 * {@link AccountType#SALARY} sinnvoll und für alle anderen Typen unnötig.
 * Das ist der Preis der Ein-Methoden-Lösung. Vermeiden liesse es sich mit
 * Überladungen (wie die drei benannten Methoden) oder einem kleinen
 * Parameterobjekt.
 * </p>
 *
 * <p>
 * Dies ist eine <em>Simple Factory</em> (eine Klasse mit einem {@code switch}),
 * nicht das Factory-Method-Pattern: Es gibt keine Creator-Unterklassen.
 * </p>
 *
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 2.0
 */
public class AccountFactory {
    /**
     * Next account id.
     */
    private long nextAccountId;

    public AccountFactory() {
        this.nextAccountId = 1000;
    }

    /**
     * Erzeugt ein Konto der gegebenen Art.
     *
     * @param type        die Kontoart
     * @param creditLimit die Kreditlimite (negativ oder 0), wird nur für {@link AccountType#SALARY} verwendet
     * @return das neue Konto
     * @throws IllegalArgumentException falls die Kreditlimite eines Lohnkontos positiv ist
     */
    public Account create(AccountType type, long creditLimit) {
        String id = type.prefix() + "-" + nextAccountId++;

        return switch (type) {
            case SAVINGS -> new SavingsAccount(id);
            case PROMO_YOUTH -> new PromoYouthSavingsAccount(id);
            case SALARY -> new SalaryAccount(id, creditLimit);
        };
    }

    public Account createPromoYouthSavingsAccount() {
        return create(AccountType.PROMO_YOUTH, 0);
    }

    public Account createSalaryAccount(long creditLimit) {
        return create(AccountType.SALARY, creditLimit);
    }

    public Account createSavingsAccount(){
        return create(AccountType.SAVINGS, 0);
    }

}
