package ch.florian.tagescode;

import org.junit.Test;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class GarminCodePacketTest {
    @Test public void limitsWatchCacheAndPreservesLeadingZeroes() {
        LocalDate today = LocalDate.of(2026, 12, 31);
        Map<String, String> codes = new HashMap<>();
        for (int i = -10; i < 100; i++) codes.put(today.plusDays(i).toString(), "012345");
        Map<?, ?> selected = (Map<?, ?>) GarminCodePacket.create(codes, today).get("codes");
        assertEquals(33, selected.size());
        assertEquals("012345", selected.get("2027-01-01"));
        assertFalse(selected.containsKey("2026-12-29"));
        assertFalse(selected.containsKey("2027-02-01"));
    }

    @Test public void missingTodayIsNotReplacedByYesterday() {
        Map<String, String> codes = new HashMap<>();
        codes.put("2026-10-01", "123456");
        codes.put("2026-10-02", "------");
        codes.put("2026-10-03", "12345");
        Map<?, ?> selected = (Map<?, ?>) GarminCodePacket.create(codes, LocalDate.of(2026,10,2)).get("codes");
        assertEquals(1, selected.size());
        assertFalse(selected.containsKey("2026-10-02"));
        assertFalse(selected.containsKey("2026-10-03"));
    }
}
