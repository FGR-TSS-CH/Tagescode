package ch.florian.tagescode;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView codeView;
    private TextView dateView;
    private Button otherDateButton;
    private Button todayButton;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat(
                    "EEEE, d. MMMM yyyy",
                    Locale.GERMANY
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.codeView);
        dateView = findViewById(R.id.dateView);
        otherDateButton =
                findViewById(R.id.otherDateButton);
        todayButton =
                findViewById(R.id.todayButton);

        otherDateButton.setOnClickListener(
                view -> openDatePicker()
        );

        todayButton.setOnClickListener(
                view -> showToday()
        );

        /*
         * Fordert beim ersten Start einmalig die nötige
         * Speicherberechtigung an.
         */
        requestFileAccessIfNeeded();

        showToday();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Beim Öffnen oder Zurückkehren in die App wird
         * PwD.txt erneut eingelesen.
         */
        showToday();
        TagescodeWidget.updateAllWidgets(this);
    }

    @Override
    protected void onPause() {
        /*
         * Beim Verlassen der App wird wieder auf den
         * heutigen Tagescode zurückgesetzt.
         */
        showToday();
        TagescodeWidget.updateAllWidgets(this);

        super.onPause();
    }

    private void openDatePicker() {
        Calendar today = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) ->
                                showSelectedDate(
                                        year,
                                        month,
                                        dayOfMonth
                                ),
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void showToday() {
        Calendar today = Calendar.getInstance();

        codeView.setText(
                CodeRepository.getCodeForToday(this)
        );

        dateView.setText(
                capitalise(
                        dateFormat.format(today.getTime())
                )
        );

        todayButton.setVisibility(View.GONE);
        otherDateButton.setVisibility(View.VISIBLE);
    }

    private void showSelectedDate(
            int year,
            int month,
            int dayOfMonth
    ) {
        Calendar selectedDate =
                Calendar.getInstance();

        selectedDate.clear();
        selectedDate.set(
                year,
                month,
                dayOfMonth
        );

        codeView.setText(
                CodeRepository.getCodeForDate(
                        this,
                        year,
                        month,
                        dayOfMonth
                )
        );

        dateView.setText(
                capitalise(
                        dateFormat.format(
                                selectedDate.getTime()
                        )
                )
        );

        otherDateButton.setVisibility(View.VISIBLE);
        todayButton.setVisibility(View.VISIBLE);
    }

    private String capitalise(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1)
                .toUpperCase(Locale.GERMANY)
                + text.substring(1);
    }

    private void requestFileAccessIfNeeded() {
        if (
                Build.VERSION.SDK_INT
                        < Build.VERSION_CODES.R
        ) {
            return;
        }

        if (Environment.isExternalStorageManager()) {
            return;
        }

        try {
            Intent appPermissionIntent =
                    new Intent(
                            Settings
                                    .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    );

            appPermissionIntent.setData(
                    Uri.parse(
                            "package:" + getPackageName()
                    )
            );

            startActivity(appPermissionIntent);

        } catch (Exception exception) {
            Intent generalPermissionIntent =
                    new Intent(
                            Settings
                                    .ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    );

            startActivity(generalPermissionIntent);
        }
    }
}
