package ch.florian.tagescode;
import org.junit.Test;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import static org.junit.Assert.*;
public class ManualCodeDateTest {
 @Test public void acceptsLeapDayAndWhitespace(){assertEquals(LocalDate.of(2024,2,29),ManualCodeDate.parse(" 29.02.2024 "));}
 @Test(expected=DateTimeParseException.class) public void rejectsInvalidLeapDay(){ManualCodeDate.parse("29.02.2025");}
 @Test(expected=DateTimeParseException.class) public void rejectsImpossibleDay(){ManualCodeDate.parse("31.04.2026");}
 @Test(expected=DateTimeParseException.class) public void rejectsWrongFormat(){ManualCodeDate.parse("2026-10-05");}
}
