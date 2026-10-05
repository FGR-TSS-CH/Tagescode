package ch.florian.tagescode;

import android.content.Context;
import android.content.SharedPreferences;
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

    private static final Object LOCK =
            new Object();

    private static final String STATUS_PREFERENCES =
            "code_import_status";

    private static final String KEY_LAST_CHECK =
            "last_check";

    private static final String KEY_LAST_SUCCESS =
            "last_success";

    private static final String KEY_LAST_FAILED =
            "last_failed";

    private static final String KEY_LAST_NEW_CODES =
            "last_new_codes";

    private static final String KEY_LAST_IMPORT_COUNT =
            "last_import_count";

    private static volatile Map<String, String> cachedCodes;

    private CodeRepository() {
    }

    static String getCodeForToday(Context context) {
        LocalDate date =
                LocalDate.now();

        return getCodeForDate(
                context,
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );
    }

    static String getCodeForDate(
            Context context,
            int year,
            int month,
            int day
    ) {
        return snapshot(context)
                .getOrDefault(
                        LocalDate.of(
                                year,
                                month + 1,
                                day
                        ).toString(),
                        "------"
                );
    }

    static AvailableCodeDates availableDates(
            Context context
    ) {
        return new AvailableCodeDates(
                snapshot(context).keySet()
        );
    }

    static Map<String, Object> garminPacket(
            Context context
    ) {
        return GarminCodePacket.create(
                snapshot(context),
                LocalDate.now()
        );
    }

    private static Map<String, String> snapshot(
            Context context
    ) {
        Map<String, String> codes =
                cachedCodes;

        if (codes == null) {
            synchronized (LOCK) {
                if (cachedCodes == null) {
                    try {
                        publish(
                                loadLocal(context)
                        );
                    } catch (IOException exception) {
                        publish(
                                new HashMap<>()
                        );
                    }
                }

                codes =
                        cachedCodes;
            }
        }

        return codes;
    }

    /**
     * Refresh the local snapshot without accessing
     * any external folder.
     */
    static void reload(Context context) {
        synchronized (LOCK) {
            try {
                publish(
                        loadLocal(context)
                );
            } catch (IOException ignored) {
                /*
                 * Preserve the last usable in-memory
                 * snapshot on a storage error.
                 */
            }
        }
    }

    /**
     * Reads the selected cloud TXT file and imports only
     * dates that are not yet stored locally.
     *
     * Returns -1 on access/read/write failure,
     * otherwise the number of newly imported dates.
     */
    static int importCloudCodes(
            Context context
    ) {
        synchronized (LOCK) {
            SharedPreferences status =
                    statusPreferences(context);

            long now =
                    System.currentTimeMillis();

            status.edit()
                    .putLong(
                            KEY_LAST_CHECK,
                            now
                    )
                    .apply();

            try {
                Map<String, String> incoming;

                try (
                        InputStream input =
                                CloudCodeFileAccess
                                        .openCodeFile(
                                                context
                                        )
                ) {
                    if (input == null) {
                        throw new IOException(
                                "No source file selected"
                        );
                    }

                    incoming =
                            CodeFileParser.read(
                                    input
                            );
                }

                if (incoming.isEmpty()) {
                    throw new IOException(
                            "No valid codes"
                    );
                }

                int added =
                        mergeAndSave(
                                context,
                                incoming
                        );

                status.edit()
                        .putLong(
                                KEY_LAST_SUCCESS,
                                now
                        )
                        .putBoolean(
                                KEY_LAST_FAILED,
                                false
                        )
                        .putInt(
                                KEY_LAST_IMPORT_COUNT,
                                added
                        )
                        .apply();

                return added;

            } catch (
                    IOException
                            | SecurityException
                            exception
            ) {
                status.edit()
                        .putBoolean(
                                KEY_LAST_FAILED,
                                true
                        )
                        .putInt(
                                KEY_LAST_IMPORT_COUNT,
                                0
                        )
                        .apply();

                return -1;
            }
        }
    }

    static boolean lastCheckFailed(
            Context context
    ) {
        return statusPreferences(context)
                .getBoolean(
                        KEY_LAST_FAILED,
                        false
                );
    }

    static long lastCheck(
            Context context
    ) {
        return statusPreferences(context)
                .getLong(
                        KEY_LAST_CHECK,
                        0
                );
    }

    static long lastSuccessfulImport(
            Context context
    ) {
        return statusPreferences(context)
                .getLong(
                        KEY_LAST_SUCCESS,
                        0
                );
    }

    static long lastNewCodesImport(
            Context context
    ) {
        return statusPreferences(context)
                .getLong(
                        KEY_LAST_NEW_CODES,
                        0
                );
    }

    static int lastImportCount(
            Context context
    ) {
        return statusPreferences(context)
                .getInt(
                        KEY_LAST_IMPORT_COUNT,
                        0
                );
    }

    static String latestAvailableDate(
            Context context
    ) {
        return snapshot(context)
                .keySet()
                .stream()
                .max(String::compareTo)
                .orElse(null);
    }

    static void invalidate() {
        synchronized (LOCK) {
            cachedCodes =
                    null;
        }
    }

    private static SharedPreferences statusPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                STATUS_PREFERENCES,
                Context.MODE_PRIVATE
        );
    }

    private static AtomicFile storage(
            Context context
    ) {
        return new AtomicFile(
                new File(
                        context.getFilesDir(),
                        "imported_tagescodes.txt"
                )
        );
    }

    private static Map<String, String> loadLocal(
            Context context
    ) throws IOException {
        Map<String, String> result;

        try (
                InputStream input =
                        storage(context)
                                .openRead()
        ) {
            result =
                    CodeFileParser.read(
                            input
                    );

        } catch (FileNotFoundException exception) {
            result =
                    new HashMap<>();
        }

        return result;
    }

    private static int mergeAndSave(
            Context context,
            Map<String, String> incoming
    ) throws IOException {
        synchronized (LOCK) {
            Map<String, String> all =
                    loadLocal(context);

            int before =
                    all.size();

            for (
                    Map.Entry<String, String> entry
                            : incoming.entrySet()
            ) {
                all.putIfAbsent(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            int added =
                    all.size() - before;

            if (added > 0) {
                AtomicFile file =
                        storage(context);

                FileOutputStream output =
                        null;

                try {
                    output =
                            file.startWrite();

                    for (
                            Map.Entry<String, String> entry
                                    : new TreeMap<>(all)
                                    .entrySet()
                    ) {
                        output.write(
                                (
                                        entry.getKey()
                                                + " "
                                                + entry.getValue()
                                                + "\n"
                                ).getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );
                    }

                    file.finishWrite(
                            output
                    );

                    statusPreferences(context)
                            .edit()
                            .putLong(
                                    KEY_LAST_NEW_CODES,
                                    System.currentTimeMillis()
                            )
                            .apply();

                } catch (IOException exception) {
                    file.failWrite(
                            output
                    );

                    throw exception;
                }
            }

            publish(all);

            return added;
        }
    }

    private static void publish(
            Map<String, String> codes
    ) {
        cachedCodes =
                Collections.unmodifiableMap(
                        new HashMap<>(codes)
                );
    }
}
