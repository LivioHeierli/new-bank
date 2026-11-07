package ch.bbw.accounts;

import ch.bbw.Booking;
import ch.bbw.Scheduled;
import ch.bbw.exceptions.InvalidAmountException;
import ch.bbw.exceptions.InvalidDateException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Konto.
 *
 * @author Luigi Cavuoti, lro@gmx.ch
 * @version 2.4
 */
public abstract class Account {
    /**
     * Anzahl Millirappen pro Franken.
     */
    private static final long MILLIRAPPEN_PER_CHF = 100_000;
    /**
     * Zeitzone für den Kontoauszug.
     */
    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");
    /**
     * Datumsformat für den Kontoauszug.
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZONE);

    /**
     * Die Kontonummer (kann auch Buchstaben und Sonderzeichen enthalten).
     */
    private final String id;
    /**
     * Die Buchungen. Einzige Quelle für den Kontostand.
     */
    private final List<Booking> bookings;

    /**
     * Erzeugt ein neues Konto.
     *
     * @param id die Kontonummer
     */
    public Account(String id) {
        this.id = id;
        this.bookings = new ArrayList<Booking>();
    }

    /**
     * Gibt die Kontonummer zurück.
     *
     * @return die Kontonummer
     */
    public String getId() {
        return id;
    }

    /**
     * Gibt die Bezeichnung des Kontotyps zurück.
     *
     * @return der Kontotyp, z.B. "Sparkonto"
     */
    public abstract String getAccountType();

    /**
     * Gibt die Buchungen als unveränderbare Liste zurück.
     *
     * @return die Buchungen (nur lesbar)
     */
    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }

    /**
     * Gibt das Saldo zurück. Es wird aus den Buchungen berechnet.
     *
     * @return Saldo in Millirappen
     */
    public long getBalance() {
        return bookings.stream()
                .mapToLong(Booking::amount)
                .sum();
    }

    /**
     * Checks if the transaction date is not in the past.
     *
     * @param date the transaction date
     * @return whether the transaction date is valid
     */
    public boolean isInvalidTransaction(Scheduled date) {
        return date.isPastDue();
    }

    /**
     * Deposits the given amount with the default text "Einzahlung".
     *
     * @param date   the transaction date
     * @param amount the amount to deposit
     * @return the new balance of the account
     */
    public long deposit(Scheduled date, long amount) throws InvalidAmountException, InvalidDateException {
        return deposit(date, amount, "Einzahlung");
    }

    /**
     * Deposits the given amount to the account.
     *
     * @param date   the transaction date
     * @param amount the amount to deposit
     * @param text   the booking text
     * @return the new balance of the account
     */
    public long deposit(Scheduled date, long amount, String text) throws InvalidAmountException, InvalidDateException {
        if (amount <= 0)
            throw new InvalidAmountException("amount must be greater than 0");

        if (isInvalidTransaction(date))
            throw new InvalidDateException("date mustn't be in the past");

        bookings.add(new Booking(date.toUnix(), amount, text));

        return getBalance();
    }

    /**
     * Withdraws the given amount with the default text "Auszahlung".
     *
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @return the new balance of the account
     */
    public long withdraw(Scheduled date, long amount) throws InvalidAmountException, InvalidDateException {
        return withdraw(date, amount, "Auszahlung");
    }

    /**
     * Hebt den gegebenen Betrag vom Konto ab.
     *
     * @param date   das Transaktionsdatum
     * @param amount der abzuhebende Betrag
     * @param text   der Buchungstext
     * @return the new balance of the account
     */
    public long withdraw(Scheduled date, long amount, String text) throws InvalidAmountException, InvalidDateException {
        if (amount <= 0)
            throw new InvalidAmountException("amount must be greater than 0");

        if (isInvalidTransaction(date))
            throw new InvalidDateException("date mustn't be in the past");

        // Achtung: hier Buchung mit negativem Betrag!
        bookings.add(new Booking(date.toUnix(), -amount, text));

        return getBalance();
    }

    /**
     * Erstellt einen Kontoauszug: eine Zeile pro Buchung mit Datum,
     * Betrag und Text, am Ende der Saldo.
     *
     * @return der Kontoauszug als Text
     */
    public String printStatement() {
        StringBuilder statement = new StringBuilder();

        statement.append("Kontoauszug ").append(id).append(System.lineSeparator());

        for (Booking booking : bookings) {
            statement.append(formatLine(booking));
        }

        statement.append("Saldo: ").append(formatAmount(getBalance())).append(System.lineSeparator());

        return statement.toString();
    }

    /**
     * Erstellt einen Kontoauszug für einen einzelnen Monat: nur die Buchungen
     * dieses Monats, am Ende der Saldo des Kontos.
     *
     * @param year  das Jahr, z.B. 2026
     * @param month der Monat (1-12)
     * @return der Kontoauszug als Text
     */
    public String printStatement(int year, int month) {
        StringBuilder statement = new StringBuilder();

        statement.append("Kontoauszug ").append(id)
                .append(String.format(" %02d.%d", month, year))
                .append(System.lineSeparator());

        for (Booking booking : bookings) {
            var date = Instant.ofEpochMilli(booking.date()).atZone(ZONE);

            if (date.getYear() == year && date.getMonthValue() == month) {
                statement.append(formatLine(booking));
            }
        }

        statement.append("Saldo: ").append(formatAmount(getBalance())).append(System.lineSeparator());

        return statement.toString();
    }

    /**
     * Formatiert eine Buchung als eine Zeile des Kontoauszugs.
     *
     * @param booking die Buchung
     * @return die Zeile inklusive Zeilenumbruch
     */
    private static String formatLine(Booking booking) {
        return String.format("%s  %15s  %s%n",
                DATE_FORMAT.format(Instant.ofEpochMilli(booking.date())),
                formatAmount(booking.amount()),
                booking.text());
    }

    /**
     * Formatiert einen Betrag in Millirappen als Frankenbetrag,
     * z.B. 123_456 als "CHF 1.23456" und -23_456 als "-CHF 0.23456".
     *
     * @param millirappen der Betrag in Millirappen
     * @return der lesbare Betrag
     */
    private static String formatAmount(long millirappen) {
        String sign = millirappen < 0 ? "-" : "";
        long absolute = Math.abs(millirappen);

        return String.format("%sCHF %d.%05d",
                sign,
                absolute / MILLIRAPPEN_PER_CHF,
                absolute % MILLIRAPPEN_PER_CHF);
    }
}