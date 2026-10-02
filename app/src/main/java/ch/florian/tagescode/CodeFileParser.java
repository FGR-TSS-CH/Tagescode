package ch.florian.tagescode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeFileParser {
    private static final Pattern ENTRY = Pattern.compile(
            "(?<![\\w./-])(\\d{1,2}/\\d{1,2}/\\d{4}|\\d{1,2}\\.\\d{1,2}\\.\\d{4}"
                    + "|\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}-\\d{1,2}-\\d{4})"
                    + "[^\\p{Alnum}\\r\\n]+([0-9]{6})(?!\\w)");
    private static final String[] FORMATS = {
            "M/d/uuuu", "d.M.uuuu", "uuuu-M-d", "d-M-uuuu"
    };

    private CodeFileParser() { }

    static Map<String, String> read(InputStream input) throws IOException {
        Map<String, String> codes = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = ENTRY.matcher(line);
                while (matcher.find()) {
                    String date = normalizeDate(matcher.group(1));
                    if (date != null) codes.putIfAbsent(date, matcher.group(2));
                }
            }
        }
        return codes;
    }

    private static String normalizeDate(String value) {
        for (String pattern : FORMATS) {
            try {
                LocalDate date = LocalDate.parse(value, DateTimeFormatter
                        .ofPattern(pattern, Locale.US).withResolverStyle(ResolverStyle.STRICT));
                if (date.getYear() > 0) return date.toString();
            } catch (DateTimeParseException ignored) { }
        }
        return null;
    }
}
