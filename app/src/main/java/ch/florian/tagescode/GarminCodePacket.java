package ch.florian.tagescode;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Small, date-keyed cache for the watch. Codes always remain strings (leading zeroes). */
final class GarminCodePacket {
    static final String APP_ID = "38e977f4f09e45ffab433147c238fed4";
    static final int DAYS_AHEAD = 31;

    static Map<String, Object> create(Map<String, String> codes, LocalDate today) {
        Map<String, String> selected = new LinkedHashMap<>();
        for (int offset = -1; offset <= DAYS_AHEAD; offset++) {
            String date = today.plusDays(offset).toString();
            String code = codes.get(date);
            if (code != null && code.matches("[0-9]{6}")) selected.put(date, code);
        }
        Map<String, Object> packet = new LinkedHashMap<>();
        packet.put("version", 1);
        packet.put("codes", selected);
        return packet;
    }
}
