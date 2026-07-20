package ch.florian.tagescode;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeRepository {
    private static final Pattern ENTRY = Pattern.compile("(?:^|\\s)(\\d{1,2}/\\d{1,2}/\\d{4})\\s+(\\d{6})(?=\\s|$)");

    private CodeRepository() {}

    static String getCodeForToday(Context context) {
        return getCodeForDate(context, new Date());
    }

    static String getCodeForDate(Context context, Date date) {
        String requestedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("tagescodes.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = ENTRY.matcher(line);
                while (matcher.find()) {
                    if (requestedDate.equals(matcher.group(1))) {
                        return matcher.group(2);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "------";
    }

    static String getCodeForDate(Context context, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, dayOfMonth);
        return getCodeForDate(context, calendar.getTime());
    }
}
