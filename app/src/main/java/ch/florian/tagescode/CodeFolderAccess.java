package ch.florian.tagescode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;

final class CodeFolderAccess {

    static final int REQUEST_CODE_FOLDER = 2001;

    private static final String PREFERENCES_NAME =
            "tagescode_storage";

    private static final String KEY_FOLDER_URI =
            "pwd_folder_uri";

    private static final String CODE_FILE_NAME =
            "PwD.txt";

    private CodeFolderAccess() {
        // Keine Instanz erforderlich.
    }

    static boolean hasSavedFolder(Context context) {
        String savedUri = getSavedFolderUri(context);

        if (savedUri == null || savedUri.isEmpty()) {
            return false;
        }

        try {
            DocumentFile folder =
                    DocumentFile.fromTreeUri(
                            context,
                            Uri.parse(savedUri)
                    );

            return folder != null
                    && folder.exists()
                    && folder.isDirectory();

        } catch (Exception ignored) {
            return false;
        }
    }

    static Intent createFolderPickerIntent() {
        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        return intent;
    }

    static boolean saveFolderAccess(
            Context context,
            Intent resultData
    ) {
        if (resultData == null) {
            return false;
        }

        Uri folderUri = resultData.getData();

        if (folderUri == null) {
            return false;
        }

        try {
            int flags =
                    resultData.getFlags()
                            & Intent.FLAG_GRANT_READ_URI_PERMISSION;

            context.getContentResolver()
                    .takePersistableUriPermission(
                            folderUri,
                            flags
                    );

            SharedPreferences preferences =
                    context.getSharedPreferences(
                            PREFERENCES_NAME,
                            Context.MODE_PRIVATE
                    );

            preferences.edit()
                    .putString(
                            KEY_FOLDER_URI,
                            folderUri.toString()
                    )
                    .apply();

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    static InputStream openCodeFile(Context context) {
        String savedUri = getSavedFolderUri(context);

        if (savedUri == null || savedUri.isEmpty()) {
            return null;
        }

        try {
            DocumentFile folder =
                    DocumentFile.fromTreeUri(
                            context,
                            Uri.parse(savedUri)
                    );

            if (
                    folder == null
                            || !folder.exists()
                            || !folder.isDirectory()
            ) {
                return null;
            }

            DocumentFile codeFile =
                    folder.findFile(CODE_FILE_NAME);

            if (
                    codeFile == null
                            || !codeFile.exists()
                            || !codeFile.isFile()
            ) {
                return null;
            }

            return context.getContentResolver()
                    .openInputStream(codeFile.getUri());

        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getSavedFolderUri(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                KEY_FOLDER_URI,
                null
        );
    }
}
