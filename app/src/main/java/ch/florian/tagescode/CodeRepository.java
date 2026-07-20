package ch.florian.tagescode;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeRepository {

    private static final String FALLBACK_ASSET_FILE = "tagescodes.txt";

    /*
     * Unterstützte Beispiele:
     * 07/20/2026 302745
     * 20.07.2026;302745
     * 2026-07-20 302745
     */
    private static final Pattern ENTRY = Pattern.compile(
            "(\\d{1,4}[./-]\\d{1,2}[./-]\\d{1,4})\\D+(\\d{6})\\b"
    );

    private static final String[] DATE_FORMATS = {
            "MM/dd/yyyy",
            "dd.MM.yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy"
    };

    private CodeRepository() {
    }

    static String getCodeForToday(Context context) {
        return getCodeForDate(context, new Date());
    }

    static String getCodeForDate(
            Context context,
            int year,
            int month,
            int dayOfMonth
    ) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, dayOfMonth);

        return getCodeForDate(context, calendar.getTime());
    }

    private static String getCodeForDate(Context context, Date requestedDate) {
        String requested = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(requestedDate);

        try (BufferedReader reader = openCodeReader(context)) {
            String line;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = ENTRY.matcher(line);

                while (matcher.find()) {
                    String date = normalizeDate(matcher.group(1));

                    if (requested.equals(date)) {
                        return matcher.group(2);
                    }
                }
            }
        } catch (Exception ignored) {
            // Bei fehlender oder unlesbarer Datei erscheint der Platzhalter.
        }

        return "------";
    }

    private static BufferedReader openCodeReader(Context context)
            throws IOException {

        File externalFile = getExternalCodeFile();

        /*
         * Die externe PwD.txt wird bei jedem Aufruf neu geöffnet.
         * Wird sie auf dem Handy ersetzt, verwendet die App automatisch
         * den neuen Inhalt.
         */
        if (externalFile.isFile()) {
            try {
                return createReader(new FileInputStream(externalFile));
            } catch (IOException | SecurityException ignored) {
                // Ohne Berechtigung oder bei einem Dateifehler wird
                // die mitgelieferte Datei verwendet.
            }
        }

        InputStream fallback =
                context.getAssets().open(FALLBACK_ASSET_FILE);

        return createReader(fallback);
    }

    private static File getExternalCodeFile() {
        File dcim = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
        );

        return new File(
                dcim,
                "Videojet/PwD/PwD.txt"
        );
    }

    private static BufferedReader createReader(InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    private static String normalizeDate(String value) {
        for (String pattern : DATE_FORMATS) {
            SimpleDateFormat parser =
                    new SimpleDateFormat(pattern, Locale.US);

            parser.setLenient(false);

            ParsePosition position = new ParsePosition(0);
            Date parsedDate = parser.parse(value, position);

            if (
                    parsedDate != null
                            && position.getIndex() == value.length()
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
