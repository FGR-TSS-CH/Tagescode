package ch.florian.tagescode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {

    private TextView codeView;
    private TextView dateView;
    private TextView codeLabelView;

    private TextView yesterdayCodeView;
    private TextView code2000View;
    private TextView code2001View;
    private TextView code2006View;

    private TextView statusDotView;
    private TextView statusTitleView;
    private TextView statusDetailView;
    private TextView buildInfoView;

    private View otherDateButton;
    private Button todayButton;
    private Button watchButton;
    private Button checkButton;
    private Button fileButton;
    private Button infoButton;

    private boolean manualCheckPending;
    private GarminConnection garmin;

    private final DateTimeFormatter longDateFormat =
            DateTimeFormatter.ofPattern(
                    "EEEE, d. MMMM yyyy",
                    Locale.GERMANY
            );

    private final DateTimeFormatter shortDateFormat =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy",
                    Locale.GERMANY
            );

    private final SimpleDateFormat timestampFormat =
            new SimpleDateFormat(
                    "dd.MM.yyyy, HH:mm",
                    Locale.GERMANY
            );

    private final ExecutorService codeExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final AtomicInteger reloadRequestNumber =
            new AtomicInteger(0);

    private boolean firstResume = true;
    private LocalDate displayedDate = LocalDate.now();
    private boolean manuallySelectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        setContentView(R.layout.activity_main);

        bindViews();
        configureButtons();
        showBuildInformation();
        showToday();
        updateStatusPanel();

        requestCloudFileAccessIfNeeded();
    }

    private void bindViews() {
        codeLabelView = findViewById(R.id.codeLabelView);
        codeView = findViewById(R.id.codeView);
        dateView = findViewById(R.id.dateView);

        yesterdayCodeView = findViewById(R.id.yesterdayCodeView);
        code2000View = findViewById(R.id.code2000View);
        code2001View = findViewById(R.id.code2001View);
        code2006View = findViewById(R.id.code2006View);

        statusDotView = findViewById(R.id.statusDotView);
        statusTitleView = findViewById(R.id.statusTitleView);
        statusDetailView = findViewById(R.id.statusDetailView);
        buildInfoView = findViewById(R.id.buildInfoView);

        otherDateButton = findViewById(R.id.otherDateButton);
        todayButton = findViewById(R.id.todayButton);

        watchButton = findViewById(R.id.watchButton);
        checkButton = findViewById(R.id.checkButton);
        fileButton = findViewById(R.id.fileButton);
        infoButton = findViewById(R.id.infoButton);
    }

    private void configureButtons() {
        codeView.setOnClickListener(view -> openDatePicker());

        codeView.setOnLongClickListener(view -> {
            String code = codeView.getText().toString();

            if (!code.matches("[0-9]{6}")) {
                Toast.makeText(
                        this,
                        R.string.no_code_to_copy,
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            }

            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);

            if (clipboard != null) {
                clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                                "Tagescode",
                                code
                        )
                );

                Toast.makeText(
                        this,
                        R.string.code_copied,
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        });

        otherDateButton.setOnClickListener(view -> openDatePicker());
        todayButton.setOnClickListener(view -> showToday());

        watchButton.setOnClickListener(view -> {
            if (garmin != null) {
                garmin.sendNow();
            } else {
                Toast.makeText(
                        this,
                        R.string.garmin_not_ready,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        checkButton.setOnClickListener(view -> startManualCheck());
        fileButton.setOnClickListener(view -> openCloudFilePicker());
        infoButton.setOnClickListener(view -> showInfoDialog());

        buildInfoView.setOnClickListener(view -> showInfoDialog());
    }

    private void startManualCheck() {
        if (manualCheckPending) {
            return;
        }

        if (!CloudCodeFileAccess.hasSavedFile(this)) {
            openCloudFilePicker();
            return;
        }

        manualCheckPending = true;
        updateStatusPanel();
        reloadCodesInBackground();
    }

    private void requestCloudFileAccessIfNeeded() {
        if (!CloudCodeFileAccess.hasSavedFile(this)) {
            openCloudFilePicker();
        }
    }

    private void openCloudFilePicker() {
        Toast.makeText(
                this,
                R.string.select_cloud_file_hint,
                Toast.LENGTH_LONG
        ).show();

        try {
            startActivityForResult(
                    CloudCodeFileAccess.createFilePickerIntent(),
                    CloudCodeFileAccess.REQUEST_CLOUD_CODE_FILE
            );
        } catch (android.content.ActivityNotFoundException exception) {
            Toast.makeText(
                    this,
                    R.string.no_file_picker,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        garmin = new GarminConnection(this);
    }

    @Override
    protected void onStop() {
        if (garmin != null) {
            garmin.close();
            garmin = null;
        }

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstResume) {
            firstResume = false;
            reloadCodesInBackground();
            return;
        }

        showToday();
        updateStatusPanel();

        TagescodeWidget.updateAllWidgets(this);

        reloadCodesInBackground();
    }

    private void openDatePicker() {
        CodeCalendarDialog.show(
                this,
                CodeRepository.availableDates(this),
                displayedDate,
                date -> showDate(date, true)
        );
    }

    private void showToday() {
        showDate(
                LocalDate.now(),
                false
        );
    }

    private void showDate(
            LocalDate date,
            boolean manuallySelected
    ) {
        displayedDate = date;
        manuallySelectedDate = manuallySelected;

        String code =
                CodeRepository.getCodeForDate(
                        this,
                        date.getYear(),
                        date.getMonthValue() - 1,
                        date.getDayOfMonth()
                );

        codeView.setText(code);

        codeLabelView.setText(
                date.equals(LocalDate.now())
                        ? R.string.code_today_label
                        : R.string.code_selected_label
        );

        dateView.setText(
                capitalise(
                        longDateFormat.format(date)
                )
        );

        todayButton.setVisibility(
                date.equals(LocalDate.now())
                        ? View.GONE
                        : View.VISIBLE
        );

        otherDateButton.setVisibility(View.VISIBLE);

        updateAdditionalCodes();
        updateStatusPanel();
    }

    private void updateAdditionalCodes() {
        LocalDate yesterday =
                LocalDate.now().minusDays(1);

        yesterdayCodeView.setText(
                getString(
                        R.string.yesterday_code_format,
                        shortDateFormat.format(yesterday),
                        getCode(yesterday)
                )
        );

        LocalDate date2000 =
                LocalDate.of(2000, 1, 1);

        code2000View.setText(
                getString(
                        R.string.fixed_code_format,
                        shortDateFormat.format(date2000),
                        getCode(date2000)
                )
        );

        LocalDate date2001 =
                LocalDate.of(2001, 1, 1);

        code2001View.setText(
                getString(
                        R.string.fixed_code_format,
                        shortDateFormat.format(date2001),
                        getCode(date2001)
                )
        );

        LocalDate date2006 =
                LocalDate.of(2006, 1, 1);

        code2006View.setText(
                getString(
                        R.string.fixed_code_format,
                        shortDateFormat.format(date2006),
                        getCode(date2006)
                )
        );
    }

    private String getCode(LocalDate date) {
        return CodeRepository.getCodeForDate(
                this,
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );
    }

    private void updateStatusPanel() {
        if (
                statusDotView == null
                        || statusTitleView == null
                        || statusDetailView == null
        ) {
            return;
        }

        int color;
        String title;
        String detail;

        boolean hasFile =
                CloudCodeFileAccess.hasSavedFile(this);

        boolean todayAvailable =
                CodeRepository
                        .getCodeForToday(this)
                        .matches("[0-9]{6}");

        if (manualCheckPending) {
            color = 0xFFF9A825;
            title = getString(R.string.status_checking);
            detail = getString(R.string.status_checking_detail);

        } else if (!hasFile) {
            color = 0xFFF9A825;
            title = getString(R.string.status_file_missing);
            detail = getString(R.string.status_file_missing_detail);

        } else if (CodeRepository.lastCheckFailed(this)) {
            color = 0xFFC62828;
            title = getString(R.string.status_error);
            detail = getString(R.string.status_error_detail);

        } else if (!todayAvailable) {
            color = 0xFFC62828;
            title = getString(R.string.status_today_missing);
            detail = getString(R.string.status_today_missing_detail);

        } else if (CodeRepository.lastSuccessfulImport(this) == 0) {
            color = 0xFFF9A825;
            title = getString(R.string.status_not_checked);
            detail = getString(R.string.status_not_checked_detail);

        } else {
            color = 0xFF2E7D32;
            title = getString(R.string.status_ok);

            long checked =
                    CodeRepository.lastSuccessfulImport(this);

            int imported =
                    CodeRepository.lastImportCount(this);

            if (imported > 0) {
                detail = getString(
                        R.string.status_ok_imported,
                        formatTimestamp(checked),
                        imported
                );
            } else {
                detail = getString(
                        R.string.status_ok_no_import,
                        formatTimestamp(checked)
                );
            }
        }

        statusDotView.setTextColor(color);
        statusTitleView.setTextColor(color);
        statusTitleView.setText(title);
        statusDetailView.setText(detail);
    }

    private void showInfoDialog() {
        String latest =
                CodeRepository.latestAvailableDate(this);

        String latestText =
                latest == null
                        ? getString(R.string.info_not_available)
                        : shortDateFormat.format(
                                LocalDate.parse(latest)
                        );

        String fileName =
                CloudCodeFileAccess.selectedFileName(this);

        long lastCheck =
                CodeRepository.lastCheck(this);

        long lastSuccess =
                CodeRepository.lastSuccessfulImport(this);

        long lastNewCodes =
                CodeRepository.lastNewCodesImport(this);

        int lastImportCount =
                CodeRepository.lastImportCount(this);

        StringBuilder message =
                new StringBuilder();

        message.append(
                getString(
                        R.string.info_version,
                        BuildConfig.VERSION_NAME
                )
        );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_build,
                                BuildConfig.BUILD_DATE
                        )
                );

        message.append("\n\n")
                .append(
                        getString(
                                R.string.info_file,
                                fileName == null
                                        ? getString(R.string.info_no_file)
                                        : fileName
                        )
                );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_last_check,
                                formatTimestampOrNever(lastCheck)
                        )
                );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_last_success,
                                formatTimestampOrNever(lastSuccess)
                        )
                );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_last_import,
                                formatTimestampOrNever(lastNewCodes)
                        )
                );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_last_import_count,
                                Math.max(lastImportCount, 0)
                        )
                );

        message.append("\n")
                .append(
                        getString(
                                R.string.info_codes_until,
                                latestText
                        )
                );

        message.append("\n\n")
                .append(getString(R.string.info_watch))
                .append("\n")
                .append(GarminTransferStatus.describe(this));

        new AlertDialog.Builder(this)
                .setTitle(R.string.info_title)
                .setMessage(message.toString())
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void showBuildInformation() {
        if (buildInfoView == null) {
            return;
        }

        buildInfoView.setText(
                getString(
                        R.string.build_info_format,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.BUILD_DATE
                )
        );
    }

    private String capitalise(String text) {
        if (
                text == null
                        || text.isEmpty()
        ) {
            return text;
        }

        return text.substring(0, 1)
                .toUpperCase(Locale.GERMANY)
                + text.substring(1);
    }

    private String formatTimestamp(long value) {
        return timestampFormat.format(
                new Date(value)
        );
    }

    private String formatTimestampOrNever(long value) {
        return value == 0
                ? getString(R.string.info_never)
                : formatTimestamp(value);
    }

    private void reloadCodesInBackground() {
        int currentRequest =
                reloadRequestNumber.incrementAndGet();

        codeExecutor.execute(() -> {
            CodeRepository.reload(
                    getApplicationContext()
            );

            int imported =
                    CodeRepository.importCloudCodes(
                            getApplicationContext()
                    );

            mainHandler.post(() -> {
                if (
                        isFinishing()
                                || isDestroyed()
                ) {
                    return;
                }

                if (
                        currentRequest
                                != reloadRequestNumber.get()
                ) {
                    return;
                }

                boolean wasManualCheck =
                        manualCheckPending;

                manualCheckPending =
                        false;

                if (wasManualCheck) {
                    if (imported < 0) {
                        Toast.makeText(
                                this,
                                R.string.check_failed_toast,
                                Toast.LENGTH_LONG
                        ).show();

                    } else if (imported > 0) {
                        Toast.makeText(
                                this,
                                getString(
                                        R.string.imported_codes_toast,
                                        imported
                                ),
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {
                        Toast.makeText(
                                this,
                                R.string.check_up_to_date,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }

                showDate(
                        manuallySelectedDate
                                ? displayedDate
                                : LocalDate.now(),
                        manuallySelectedDate
                );

                updateStatusPanel();

                TagescodeWidget.updateAllWidgets(this);

                if (garmin != null) {
                    garmin.sync();
                }
            });
        });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode
                        == CloudCodeFileAccess
                        .REQUEST_CLOUD_CODE_FILE
        ) {
            if (resultCode == RESULT_OK) {
                if (
                        CloudCodeFileAccess
                                .saveFileAccess(
                                        this,
                                        data
                                )
                ) {
                    manualCheckPending = true;
                    updateStatusPanel();
                    reloadCodesInBackground();

                } else {
                    Toast.makeText(
                            this,
                            R.string.file_access_not_saved,
                            Toast.LENGTH_LONG
                    ).show();
                }

            } else {
                Toast.makeText(
                        this,
                        R.string.file_selection_cancelled,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        codeExecutor.shutdownNow();

        if (garmin != null) {
            garmin.close();
        }

        super.onDestroy();
    }
}
