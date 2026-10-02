package ch.florian.tagescode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;

final class CloudCodeFileAccess {
    static final int REQUEST_CLOUD_CODE_FILE = 2002;
    private static final String KEY_URI = "cloud_code_file_uri";

    private CloudCodeFileAccess() { }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("tagescode_cloud_storage", Context.MODE_PRIVATE);
    }

    static boolean hasSavedFile(Context context) {
        // An offline provider must not cause the selection to be forgotten.
        return preferences(context).contains(KEY_URI);
    }

    static Intent createFilePickerIntent() {
        return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/plain")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    }

    static boolean saveFileAccess(Context context, Intent data) {
        if (data == null || data.getData() == null
                || (data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) return false;
        Uri uri = data.getData();
        try {
            context.getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String oldUri = preferences(context).getString(KEY_URI, null);
            preferences(context).edit().putString(KEY_URI, uri.toString()).apply();
            if (oldUri != null && !oldUri.equals(uri.toString())) {
                try {
                    context.getContentResolver().releasePersistableUriPermission(
                            Uri.parse(oldUri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) { }
            }
            return true;
        } catch (SecurityException | IllegalArgumentException exception) {
            return false;
        }
    }

    static InputStream openCodeFile(Context context) throws IOException {
        String value = preferences(context).getString(KEY_URI, null);
        if (value == null) return null;
        try {
            InputStream input = context.getContentResolver().openInputStream(Uri.parse(value));
            if (input == null) throw new IOException("Dateianbieter nicht verfügbar");
            return input;
        } catch (SecurityException | IllegalArgumentException exception) {
            throw new IOException("Dateizugriff nicht verfügbar", exception);
        }
    }
}
