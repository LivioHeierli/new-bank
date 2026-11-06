package ch.bbw;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

/**
 * A class to represent a scheduled date.
 * @author lro@gmx.ch
 * @version 1.0
 */
public class Scheduled {

    /**
     * The milliseconds until the date is due. Negative values mean that the date is past due.
     */
    private long millisecondsUntilDue;
    public Scheduled(long millisecondsUntilDue) {
        this.millisecondsUntilDue = millisecondsUntilDue;
    }

    /**
     * Checks if the transaction date is not in the past.
     * @return true if the transaction date is not in the past.
     */
    public boolean isPastDue() {
        return millisecondsUntilDue < 0;
    }

    /**
     * Creates a new Scheduled date with the current instant.
     * @return a new Scheduled date with the current instant.
     */
    public static Scheduled now() {
        return new Scheduled(0);
    }

    /**
     * Converts the Scheduled date to a unix timestamp.
     * @return the unix timestamp
     */
    public long toUnix(){
        return toInstant().toEpochMilli();
    }

    /**
     * Converts the Scheduled date to an Instant.
     * @return the Instant
     */
    public Instant toInstant(){

        return Instant.now().plus(millisecondsUntilDue, ChronoUnit.MILLIS);
    }

    /**
     * Adds a duration to the Scheduled date.
     * @param duration the duration to add
     * @param unit the unit of the duration
     * @return the Scheduled date
     */
    public Scheduled plus(long duration, ChronoUnit unit){
            return switch (unit) {
                case MILLIS    -> plusMillis(duration);
                case SECONDS   -> plusSeconds(duration);
                case MINUTES   -> plusMinutes(duration);
                case HOURS     -> plusHours(duration);
                case DAYS      -> plusDays(duration);
                case WEEKS -> plusWeeks(duration);
                case MONTHS    -> plusMonths(duration);
                case YEARS     -> plusYears(duration);
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit + "\nI guess you have to implement it yourself ;)");
            };
    }

    public Scheduled plusMillis(long millisToAdd){
        millisecondsUntilDue += millisToAdd;
        return this;
    }

    public Scheduled plusSeconds(long secondsToAdd){
        millisecondsUntilDue += secondsToAdd * 1000;
        return this;
    }

    public Scheduled plusMinutes(long minutesToAdd){
        millisecondsUntilDue += minutesToAdd * 1000 * 60;
        return this;
    }

    public Scheduled plusHours(long hoursToAdd){
        millisecondsUntilDue += hoursToAdd * 1000 * 60 * 60;
        return this;
    }

    public Scheduled plusDays(long daysToAdd){
        millisecondsUntilDue += daysToAdd * 1000 * 60 * 60 * 24;
        return this;
    }

    public Scheduled plusWeeks(long weeksToAdd){
        millisecondsUntilDue += weeksToAdd * 1000 * 60 * 60 * 24 * 7;
        return this;
    }

    public Scheduled plusMonths(long monthsToAdd){
        millisecondsUntilDue += monthsToAdd * 1000 * 60 * 60 * 24 * 30;
        return this;
    }

    public Scheduled plusYears(long yearsToAdd){
        millisecondsUntilDue += yearsToAdd * 1000 * 60 * 60 * 24 * 365;
        return this;
    }

    /**
     * Subtracts a duration from the Scheduled date.
     * @param duration the duration to subtract
     * @param unit the unit of the duration
     * @return the Scheduled date
     */
    public Scheduled minus(long duration, ChronoUnit unit){
        return switch (unit) {
            case MILLIS    -> minusMillis(duration);
            case SECONDS   -> minusSeconds(duration);
            case MINUTES   -> minusMinutes(duration);
            case HOURS     -> minusHours(duration);
            case DAYS      -> minusDays(duration);
            case WEEKS -> minusWeeks(duration);
            case MONTHS    -> minusMonths(duration);
            case YEARS     -> minusYears(duration);
            default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit + "\nI guess you have to implement it yourself ;)");
        };
    }

    public Scheduled minusMillis(long millisToAdd){
        millisecondsUntilDue -= millisToAdd;
        return this;
    }

    public Scheduled minusSeconds(long secondsToAdd){
        millisecondsUntilDue -= secondsToAdd * 1000;
        return this;
    }

    public Scheduled minusMinutes(long minutesToAdd){
        millisecondsUntilDue -= minutesToAdd * 1000 * 60;
        return this;
    }

    public Scheduled minusHours(long hoursToAdd){
        millisecondsUntilDue -= hoursToAdd * 1000 * 60 * 60;
        return this;
    }

    public Scheduled minusDays(long daysToAdd){
        millisecondsUntilDue -= daysToAdd * 1000 * 60 * 60 * 24;
        return this;
    }

    public Scheduled minusWeeks(long weeksToAdd){
        millisecondsUntilDue -= weeksToAdd * 1000 * 60 * 60 * 24 * 7;
        return this;
    }

    public Scheduled minusMonths(long monthsToAdd){
        millisecondsUntilDue -= monthsToAdd * 1000 * 60 * 60 * 24 * 30;
        return this;
    }

    public Scheduled minusYears(long yearsToAdd){
        millisecondsUntilDue -= yearsToAdd * 1000 * 60 * 60 * 24 * 365;
        return this;
    }

    /**
     * Converts the Scheduled date to a String.
     * @return the String representation of the Scheduled date
     */
    @Override
    public String toString() {
        return toInstant().toString();
    }
}
