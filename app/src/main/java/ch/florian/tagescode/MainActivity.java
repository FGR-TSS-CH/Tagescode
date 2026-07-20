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

    private TextView yesterdayCodeView;
    private TextView code2000View;
    private TextView code2001View;
    private TextView code2006View;
    private TextView buildInfoView;

    private Button otherDateButton;
    private Button todayButton;

    private final SimpleDateFormat longDateFormat =
            new SimpleDateFormat(
                    "EEEE, d. MMMM yyyy",
                    Locale.GERMANY
            );

    private final SimpleDateFormat shortDateFormat =
            new SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.GERMANY
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.codeView);
        dateView = findViewById(R.id.dateView);

        yesterdayCodeView =
                findViewById(R.id.yesterdayCodeView);

        code2000View =
                findViewById(R.id.code2000View);

        code2001View =
                findViewById(R.id.code2001View);

        code2006View =
                findViewById(R.id.code2006View);

        otherDateButton =
                findViewById(R.id.otherDateButton);

        todayButton =
                findViewById(R.id.todayButton);

        buildInfoView =
        findViewById(R.id.buildInfoView);

showBuildInformation();

        otherDateButton.setOnClickListener(
                view -> openDatePicker()
        );

        todayButton.setOnClickListener(
                view -> showToday()
        );

        requestFileAccessIfNeeded();
        showToday();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showToday();
        TagescodeWidget.updateAllWidgets(this);
    }

    @Override
    protected void onPause() {
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
                        longDateFormat.format(today.getTime())
                )
        );

        todayButton.setVisibility(View.GONE);
        otherDateButton.setVisibility(View.VISIBLE);

        updateAdditionalCodes();
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
                        longDateFormat.format(
                                selectedDate.getTime()
                        )
                )
        );

        otherDateButton.setVisibility(View.VISIBLE);
        todayButton.setVisibility(View.VISIBLE);

        updateAdditionalCodes();
    }

    private void updateAdditionalCodes() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        String yesterdayDate =
                shortDateFormat.format(yesterday.getTime());

        String yesterdayCode =
                CodeRepository.getCodeForDate(
                        this,
                        yesterday.get(Calendar.YEAR),
                        yesterday.get(Calendar.MONTH),
                        yesterday.get(Calendar.DAY_OF_MONTH)
                );

        yesterdayCodeView.setText(
                getString(
                        R.string.yesterday_code_format,
                        yesterdayDate,
                        yesterdayCode
                )
        );

        code2000View.setText(
                getString(
                        R.string.fixed_code_format,
                        "01.01.2000",
                        CodeRepository.getCodeForDate(
                                this,
                                2000,
                                Calendar.JANUARY,
                                1
                        )
                )
        );

        code2001View.setText(
                getString(
                        R.string.fixed_code_format,
                        "01.01.2001",
                        CodeRepository.getCodeForDate(
                                this,
                                2001,
                                Calendar.JANUARY,
                                1
                        )
                )
        );

        code2006View.setText(
                getString(
                        R.string.fixed_code_format,
                        "01.01.2006",
                        CodeRepository.getCodeForDate(
                                this,
                                2006,
                                Calendar.JANUARY,
                                1
                        )
                )
        );
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
private void showBuildInformation() {
    buildInfoView.setText(
            getString(
                    R.string.build_info_format,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.BUILD_DATE
            )
    );
}
}
