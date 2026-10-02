package ch.florian.tagescode;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Records SDK delivery confirmation, not a confirmation of the watch display. */
final class GarminTransferStatus {
    static void record(Context context, boolean success, String detail) {
        SharedPreferences prefs = context.getSharedPreferences("garmin", Context.MODE_PRIVATE);
        if (!prefs.contains("device")) return;
        long now = System.currentTimeMillis();
        SharedPreferences.Editor edit = prefs.edit()
            .putLong("transfer_attempt", now).putBoolean("transfer_ok", success)
            .putString("transfer_detail", detail);
        if (success) edit.putLong("transfer_success", now);
        edit.apply();
    }

    static String describe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("garmin", Context.MODE_PRIVATE);
        if (!prefs.contains("device")) return "Keine Uhr ausgewählt. Bitte zuerst eine Uhr auswählen.";
        long success = prefs.getLong("transfer_success", prefs.getLong("last_background_transfer", 0));
        long attempt = prefs.getLong("transfer_attempt", 0);
        String result = "Letzte erfolgreiche Übertragung:\n" + date(success);
        if (attempt > 0) result += "\n\nLetzter Versuch: " + date(attempt)
            + "\n" + (prefs.getBoolean("transfer_ok", false) ? "Erfolgreich" : "Nicht erfolgreich")
            + "\n" + prefs.getString("transfer_detail", "");
        return result + "\n\nErfolg bedeutet: Garmin hat die Übertragung bestätigt. Die Anzeige auf der Uhr wird damit nicht geprüft.";
    }

    private static String date(long value) {
        return value == 0 ? "Noch nicht bestätigt" : new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMAN).format(new Date(value));
    }
}
