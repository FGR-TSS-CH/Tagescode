package ch.florian.tagescode;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeRepository {

    private static final String FALLBACK_ASSET_FILE =
            "tagescodes.txt";

    private static final Pattern ENTRY_PATTERN =
            Pattern.compile(
                    "(\\d{1,4}[./-]\\d{1,2}[./-]\\d{1,4})"
                            + "\\D+"
                            + "(\\d{6})\\b"
            );

    private static final String[] SUPPORTED_DATE_FORMATS = {
            "MM/dd/yyyy",
            "dd.MM.yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy"
    };

    private static final Object CACHE_LOCK =
            new Object();

    /*
     * Die komplette Codeliste wird nur einmal geladen.
     * Danach erfolgen alle Abfragen direkt aus dieser Map.
     */
    private static volatile Map<String, String> cachedCodes =
            Collections.emptyMap();

    private static volatile boolean cacheLoaded = false;

    private CodeRepository() {
        // Keine Instanz erforderlich.
    }

    static String getCodeForToday(Context context) {
        java.util.Calendar today =
                java.util.Calendar.getInstance();

        return getCodeForDate(
                context,
                today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH),
                today.get(java.util.Calendar.DAY_OF_MONTH)
        );
    }

    static String getCodeForDate(
            Context context,
            int year,
            int month,
            int dayOfMonth
    ) {
        ensureLoaded(context);

        String isoDate =
                String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                );

        String code = cachedCodes.get(isoDate);

        if (code == null || code.isEmpty()) {
            return "------";
        }

        return code;
    }

    /**
     * Lädt die Datei erneut. Während des Ladens bleibt
     * der bisherige Cache verwendbar.
     */
    static void reload(Context context) {
        Context applicationContext =
                context.getApplicationContext();

        Map<String, String> loadedCodes =
                loadCodes(applicationContext);

        synchronized (CACHE_LOCK) {
            cachedCodes =
                    Collections.unmodifiableMap(
                            new HashMap<>(loadedCodes)
                    );

            cacheLoaded = true;
        }
    }

    /**
     * Wird verwendet, nachdem ein anderer Ordner
     * ausgewählt wurde.
     */
    static void invalidate() {
        synchronized (CACHE_LOCK) {
            cachedCodes = Collections.emptyMap();
            cacheLoaded = false;
        }
    }

    private static void ensureLoaded(Context context) {
        if (cacheLoaded) {
            return;
        }

        synchronized (CACHE_LOCK) {
            if (cacheLoaded) {
                return;
            }

            Map<String, String> loadedCodes =
                    loadCodes(
                            context.getApplicationContext()
                    );

            cachedCodes =
                    Collections.unmodifiableMap(
                            new HashMap<>(loadedCodes)
                    );

            cacheLoaded = true;
        }
    }

    private static Map<String, String> loadCodes(
            Context context
    ) {
        /*
         * Zuerst wird die externe PwD.txt gelesen.
         */
        InputStream externalInputStream =
                CodeFolderAccess.openCodeFile(context);

        if (externalInputStream != null) {
            Map<String, String> externalCodes =
                    readEntries(externalInputStream);

            if (!externalCodes.isEmpty()) {
                return externalCodes;
            }
        }

        /*
         * Falls die externe Datei fehlt oder ungültig ist,
         * wird die integrierte Datei verwendet.
         */
        try {
            InputStream fallbackInputStream =
                    context.getAssets()
                            .open(FALLBACK_ASSET_FILE);

            return readEntries(fallbackInputStream);

        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    private static Map<String, String> readEntries(
            InputStream inputStream
    ) {
        Map<String, String> result =
                new HashMap<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                Matcher matcher =
                        ENTRY_PATTERN.matcher(line);

                while (matcher.find()) {
                    String normalizedDate =
                            normalizeDate(
                                    matcher.group(1)
                            );

                    if (normalizedDate != null) {
                        result.put(
                                normalizedDate,
                                matcher.group(2)
                        );
                    }
                }
            }

        } catch (Exception ignored) {
            // Bei einem Lesefehler wird eine leere Map geliefert.
        }

        return result;
    }

    private static String normalizeDate(
            String inputDate
    ) {
        if (
                inputDate == null
                        || inputDate.isEmpty()
        ) {
            return null;
        }

        for (String pattern : SUPPORTED_DATE_FORMATS) {
            SimpleDateFormat parser =
                    new SimpleDateFormat(
                            pattern,
                            Locale.US
                    );

            parser.setLenient(false);

            ParsePosition parsePosition =
                    new ParsePosition(0);

            Date parsedDate =
                    parser.parse(
                            inputDate,
                            parsePosition
                    );

            if (
                    parsedDate != null
                            && parsePosition.getIndex()
                            == inputDate.length()
            ) {
                return new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                ).format(parsedDate);
            }
        }

        return null;
    }
}
