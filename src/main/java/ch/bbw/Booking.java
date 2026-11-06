package ch.bbw;

/**
 * Buchung.
 * <p>
 * Entscheid zum Text: Ein Text von {@code null}, leer oder nur aus Leerzeichen
 * wird mit einer {@link IllegalArgumentException} abgelehnt, damit jede Buchung
 * sagt, warum Geld bewegt wurde. Die Standardtexte liefert der Aufrufer
 * (z.B. {@code Account}).
 * <p>
 * Entscheid zum Datum: Das Datum ist ein Unix-Zeitstempel in Millisekunden
 * seit 1.1.1970 (so wie {@code Scheduled.toUnix()} ihn liefert).
 *
 * @param date   Datum der Transaktion (Unix-Zeitstempel in Millisekunden).
 * @param amount Transaktionsbetrag (Millirappen).
 * @param text   Beschreibung der Buchung.
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 2.2
 */
public record Booking(long date, long amount, String text) {
	/**
	 * Erzeugt eine neue Buchung.
	 *
	 * @throws IllegalArgumentException falls der Text {@code null} oder leer ist
	 */
	public Booking {
		if (text == null || text.isBlank())
			throw new IllegalArgumentException("text must not be null or blank");
	}
}