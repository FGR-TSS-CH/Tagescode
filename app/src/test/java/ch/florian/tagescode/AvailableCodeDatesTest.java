package ch.florian.tagescode;

import org.junit.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class AvailableCodeDatesTest {
    @Test public void gapsAreNotSelectable() {
        AvailableCodeDates dates = new AvailableCodeDates(Arrays.asList("2026-10-01", "2026-10-03"));
        assertTrue(dates.contains(LocalDate.of(2026, 10, 1)));
        assertFalse(dates.contains(LocalDate.of(2026, 10, 2)));
        assertFalse(dates.contains(LocalDate.of(2026, 9, 30)));
        assertFalse(dates.contains(LocalDate.of(2026, 10, 4)));
    }
    @Test public void skipsEmptyMonthsAndHandlesYearBoundary() {
        AvailableCodeDates dates = new AvailableCodeDates(Arrays.asList("2027-01-02", "2000-01-01", "2026-10-03"));
        assertEquals(Arrays.asList(YearMonth.of(2000, 1), YearMonth.of(2026, 10), YearMonth.of(2027, 1)), dates.months());
        assertEquals(1, dates.initialMonth(LocalDate.of(2026, 12, 1)));
        assertEquals(0, dates.initialMonth(LocalDate.of(1999, 1, 1)));
        assertEquals(2, dates.initialMonth(LocalDate.of(2028, 1, 1)));
    }
    @Test public void emptyListAndLeapDay() {
        assertEquals(-1, new AvailableCodeDates(Collections.emptyList()).initialMonth(LocalDate.now()));
        AvailableCodeDates dates = new AvailableCodeDates(Collections.singletonList("2024-02-29"));
        assertTrue(dates.contains(LocalDate.of(2024, 2, 29)));
        assertFalse(dates.contains(LocalDate.of(2024, 2, 28)));
    }
}
