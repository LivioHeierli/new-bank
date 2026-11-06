import ch.bbw.Booking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests für die Klasse Booking.
 *
 * @author Luigi Cavuoti
 * @version 1.1
 */
public class BookingTests
{
	/**
	 * Tests für die Erzeugung von Buchungen.
	 */
	@Test
	@DisplayName("Construct Simple Booking, should work ")
	public void testInitialization()
	{
		Booking booking = new Booking(1_000L, 5_000L, "Einzahlung");

		assertEquals(1_000L, booking.date());
		assertEquals(5_000L, booking.amount());
		assertEquals("Einzahlung", booking.text());
	}

	/**
	 * Eine Buchung ohne Text (null) wird abgelehnt.
	 */
	@Test
	@DisplayName("Booking with null text should be rejected")
	public void testNullTextRejected()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new Booking(1_000L, 5_000L, null));
	}

	/**
	 * Eine Buchung mit leerem oder nur aus Leerzeichen bestehendem Text wird abgelehnt.
	 */
	@Test
	@DisplayName("Booking with blank text should be rejected")
	public void testBlankTextRejected()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new Booking(1_000L, 5_000L, ""));
		assertThrows(IllegalArgumentException.class,
				() -> new Booking(1_000L, 5_000L, "   "));
	}

	/**
	 * Die Textdarstellung eines Records enthält alle Werte.
	 */
	@Test
	@DisplayName("Booking toString should contain all values")
	public void testPrint()
	{
		Booking booking = new Booking(1_000L, 5_000L, "Einzahlung");

		String text = booking.toString();

		assertTrue(text.contains("1000"));
		assertTrue(text.contains("5000"));
		assertTrue(text.contains("Einzahlung"));
	}
}