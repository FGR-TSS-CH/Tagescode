package ch.florian.tagescode;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.Assert.*;

public class CodeFileParserTest {
    private Map<String, String> parse(String text) throws IOException {
        return CodeFileParser.read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test public void importsPowerAutomateDatesAndLeadingZeros() throws Exception {
        Map<String, String> codes = parse("\uFEFF09/28/2026 230550\r\n10/02/2026 000416\r\nAllzeit viel Erfolg");
        assertEquals(2, codes.size());
        assertEquals("000416", codes.get("2026-10-02"));
        assertEquals("230550", codes.get("2026-09-28"));
    }

    @Test public void firstEntryWinsAcrossDateFormats() throws Exception {
        Map<String, String> codes = parse("10/02/2026 000416\n02.10.2026 999999\n2026-10-02 888888");
        assertEquals(1, codes.size());
        assertEquals("000416", codes.get("2026-10-02"));
    }

    @Test public void rejectsInvalidDatesAndMalformedCodes() throws Exception {
        assertTrue(parse("02/29/2025 123456\n04/31/2026 123456\n10/02/2026 1234567\n"
                + "10/03/2026 12345\n10/04/2026 123456x\n110/05/2026 123456\n"
                + "10/06/2026 text 123456\n2026-10-07 12345678").isEmpty());
    }

    @Test public void acceptsLeapDayAndLocalFormats() throws Exception {
        Map<String, String> codes = parse("02/29/2024 000001\n1.1.2000 000002\n1-1-2001 000003");
        assertEquals(3, codes.size());
        assertEquals("000001", codes.get("2024-02-29"));
    }

    @Test public void slashDatesAreMonthFirst() throws Exception {
        assertEquals("123456", parse("10/02/2026 123456").get("2026-10-02"));
        assertTrue(parse("13/02/2026 123456").isEmpty());
    }

    @Test public void readFailureDoesNotReturnPartialImport() throws Exception {
        InputStream broken = new InputStream() {
            final byte[] prefix = "10/02/2026 123456\n".getBytes(StandardCharsets.UTF_8);
            int position;
            @Override public int read() throws IOException {
                if (position == prefix.length) throw new IOException("Provider disconnected");
                return prefix[position++];
            }
        };
        assertThrows(IOException.class, () -> CodeFileParser.read(broken));
    }
}
