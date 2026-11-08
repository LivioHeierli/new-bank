package ch.bbw;

/**
 * Die Kontoarten der Bank. Hält Typ und Id-Präfix an einem Ort zusammen,
 * damit das Präfix nicht mehr vom Typ wegdriften kann.
 *
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 1.0
 */
public enum AccountType {
    /**
     * Sparkonto.
     */
    SAVINGS("S"),
    /**
     * Promo-Jugendsparkonto.
     */
    PROMO_YOUTH("Y"),
    /**
     * Lohnkonto.
     */
    SALARY("P");

    private final String prefix;

    AccountType(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gibt das Präfix der Kontonummer zurück (ohne Bindestrich), z.B. "S".
     *
     * @return das Präfix
     */
    public String prefix() {
        return prefix;
    }
}
