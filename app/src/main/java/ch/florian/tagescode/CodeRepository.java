package ch.florian.tagescode;

import android.content.Context;
import android.util.AtomicFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

final class CodeRepository {
    private static final Object LOCK = new Object();
    private static volatile Map<String, String> cachedCodes;

    private CodeRepository() { }

    static String getCodeForToday(Context context) {
        LocalDate date = LocalDate.now();
        return getCodeForDate(context, date.getYear(), date.getMonthValue() - 1,
                date.getDayOfMonth());
    }

    static String getCodeForDate(Context context, int year, int month, int day) {
        return snapshot(context).getOrDefault(LocalDate.of(year, month + 1, day).toString(), "------");
    }

    static AvailableCodeDates availableDates(Context context) {
        return new AvailableCodeDates(snapshot(context).keySet());
    }

    private static Map<String, String> snapshot(Context context) {
        Map<String, String> codes = cachedCodes;
        if (codes == null) {
            synchronized (LOCK) {
                if (cachedCodes == null) {
                    try { publish(loadLocal(context)); }
                    catch (IOException exception) { publish(new HashMap<>()); }
                }
                codes = cachedCodes;
            }
        }
        return codes;
    }

    /** Refresh the local snapshot without accessing any external folder. */
    static void reload(Context context) {
        synchronized (LOCK) {
            try { publish(loadLocal(context)); }
            catch (IOException ignored) {
                // Preserve the last usable in-memory snapshot on a storage error.
            }
        }
    }

    /** Returns -1 on access/read/write failure, otherwise the number of new dates. */
    static int importCloudCodes(Context context) {
        synchronized (LOCK) {
            try {
                Map<String, String> incoming;
                try (InputStream input = CloudCodeFileAccess.openCodeFile(context)) {
                    if (input == null) throw new IOException("No source file selected");
                    incoming = CodeFileParser.read(input);
                }
                if (incoming.isEmpty()) throw new IOException("No valid codes");
                int added = mergeAndSave(context, incoming);
                context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                        .edit().putLong("last_success", System.currentTimeMillis())
                        .putBoolean("last_failed", false).apply();
                return added;
            } catch (IOException | SecurityException exception) {
                context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                        .edit().putBoolean("last_failed", true).apply();
                return -1;
            }
        }
    }

    static boolean lastCheckFailed(Context context) {
        return context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                .getBoolean("last_failed", false);
    }

    static long lastSuccessfulImport(Context context) {
        return context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                .getLong("last_success", 0);
    }

    static long lastNewCodesImport(Context context) {
        return context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                .getLong("last_new_codes", 0);
    }

    static String latestAvailableDate(Context context) {
        return snapshot(context).keySet().stream().max(String::compareTo).orElse(null);
    }

    static void invalidate() {
        synchronized (LOCK) { cachedCodes = null; }
    }

    private static AtomicFile storage(Context context) {
        return new AtomicFile(new File(context.getFilesDir(), "imported_tagescodes.txt"));
    }

    private static Map<String, String> loadLocal(Context context) throws IOException {
        Map<String, String> result;
        try (InputStream input = storage(context).openRead()) {
            result = CodeFileParser.read(input);
        } catch (FileNotFoundException exception) {
            result = new HashMap<>();
        }
        return result;
    }

    private static int mergeAndSave(Context context, Map<String, String> incoming)
            throws IOException {
        synchronized (LOCK) {
            Map<String, String> all = loadLocal(context);
            int before = all.size();
            for (Map.Entry<String, String> entry : incoming.entrySet()) {
                all.putIfAbsent(entry.getKey(), entry.getValue());
            }
            int added = all.size() - before;
            if (added > 0) {
                AtomicFile file = storage(context);
                FileOutputStream output = null;
                try {
                    output = file.startWrite();
                    for (Map.Entry<String, String> entry : new TreeMap<>(all).entrySet()) {
                        output.write((entry.getKey() + " " + entry.getValue() + "\n")
                                .getBytes(StandardCharsets.UTF_8));
                    }
                    file.finishWrite(output);
                    context.getSharedPreferences("code_import_status", Context.MODE_PRIVATE)
                            .edit().putLong("last_new_codes", System.currentTimeMillis()).apply();
                } catch (IOException exception) {
                    file.failWrite(output);
                    throw exception;
                }
            }
            publish(all);
            return added;
        }
    }

    private static void publish(Map<String, String> codes) {
        cachedCodes = Collections.unmodifiableMap(new HashMap<>(codes));
    }
}
