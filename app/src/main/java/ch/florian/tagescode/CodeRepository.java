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

    /*
     * Datei, die mit der APK mitgeliefert wird.
     * Sie wird verwendet, falls die externe PwD.txt fehlt
     * oder nicht gelesen werden kann.
     */
    private static final String FALLBACK_ASSET_FILE = "tagescodes.txt";

    /*
     * Fester Speicherort:
     * Interner Speicher/DCIM/Videojet/PwD/PwD.txt
     */
    private static final String EXTERNAL_FOLDER = "Videojet/PwD";
    private static final String EXTERNAL_FILE = "PwD.txt";

    /*
     * Unterstützte Beispiele:
     *
     * 07/20/2026 302745
     * 20.07.2026;302745
     * 2026-07-20 302745
     * 20/07/2026 = 302745
     */
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "(\\d{1,4}[./-]\\d{1,2}[./-]\\d{1,4})\\D+(\\d{6})\\b"
    );

    private static final String[] SUPPORTED_DATE_FORMATS = {
            "MM/dd/yyyy",
            "dd.MM.yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy"
    };

    private CodeRepository() {
        // Keine Instanzen dieser Klasse nötig.
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

    private static String getCodeForDate(
            Context context,
            Date requestedDate
    ) {
        String requestedDateString = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(requestedDate);

        try (BufferedReader reader = openCodeReader(context)) {
            String line;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = ENTRY_PATTERN.matcher(line);

                while (matcher.find()) {
                    String normalizedDate =
                            normalizeDate(matcher.group(1));

                    if (requestedDateString.equals(normalizedDate)) {
                        return matcher.group(2);
                    }
                }
            }
        } catch (IOException | SecurityException ignored) {
            // Bei einem Fehler wird der Platzhalter angezeigt.
        }

        return "------";
    }

    private static BufferedReader openCodeReader(Context context)
            throws IOException {

        File externalFile = getExternalCodeFile();

        /*
         * Die externe PwD.txt wird bei jedem Aufruf neu geöffnet.
         * Wird die Datei ersetzt, verwendet die App automatisch
         * die neue Version.
         */
        if (externalFile.isFile() && externalFile.length() > 0) {
            try {
                return createReader(
                        new FileInputStream(externalFile)
                );
            } catch (IOException | SecurityException ignored) {
                /*
                 * Falls die externe Datei nicht gelesen werden kann,
                 * wird die mitgelieferte Datei verwendet.
                 */
            }
        }

        InputStream fallbackInputStream =
                context.getAssets().open(FALLBACK_ASSET_FILE);

        return createReader(fallbackInputStream);
    }

    private static File getExternalCodeFile() {
        File dcimFolder =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                );

        File pwdFolder = new File(
                dcimFolder,
                EXTERNAL_FOLDER
        );

        return new File(
                pwdFolder,
                EXTERNAL_FILE
        );
    }

    private static BufferedReader createReader(
            InputStream inputStream
    ) {
        return new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    private static String normalizeDate(String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) {
            return null;
        }

        for (String pattern : SUPPORTED_DATE_FORMATS) {
            SimpleDateFormat parser =
                    new SimpleDateFormat(pattern, Locale.US);

            parser.setLenient(false);

            ParsePosition parsePosition =
                    new ParsePosition(0);

            Date parsedDate =
                    parser.parse(inputDate, parsePosition);

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
