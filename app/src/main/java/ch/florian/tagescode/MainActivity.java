package ch.florian.tagescode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity {

    private TextView codeView;
    private TextView dateView;
    private TextView codeLabelView;
    private TextView dataStatusView;
    private TextView availabilityView;
    private TextView checkStatusView;
    private boolean manualCheckPending;
    private GarminConnection garmin;

    private TextView yesterdayCodeView;
    private TextView code2000View;
    private TextView code2001View;
    private TextView code2006View;

    private TextView buildInfoView;

    /*
     * Der blaue Datumsbutton ist im aktuellen Layout
     * ein LinearLayout. Deshalb wird er als View behandelt.
     */
    private View otherDateButton;

    private Button todayButton;

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

    /*
     * Das erneute Einlesen der lokalen Codeliste erfolgt in einem
     * Hintergrundthread. Die Bedienoberfläche bleibt
     * dadurch jederzeit reaktionsfähig.
     */
    private final ExecutorService codeExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    /*
     * Jede angeforderte Aktualisierung erhält eine Nummer.
     * Nur das Ergebnis der zuletzt angeforderten
     * Aktualisierung wird auf der Oberfläche angezeigt.
     */
    private final AtomicInteger reloadRequestNumber =
            new AtomicInteger(0);

    private BottomControls bottomControls;
    private boolean loadingCodes;
    private boolean firstResume = true;
    private LocalDate displayedDate = LocalDate.now();
    private boolean manuallySelectedDate;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        int mode = base.getSharedPreferences("appearance", MODE_PRIVATE).getInt("mode", 0);
        if (mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                || mode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            android.content.res.Configuration config = new android.content.res.Configuration(base.getResources().getConfiguration());
            config.uiMode = (config.uiMode & ~android.content.res.Configuration.UI_MODE_NIGHT_MASK) | mode;
            base = base.createConfigurationContext(config);
        }
        super.attachBaseContext(base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Tagescode);
        super.onCreate(savedInstanceState);
        // Use one consistent inset model; FitScreenLayout reserves the system bars.
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

        setContentView(
                R.layout.activity_main
        );

        bindViews();

        configureButtons();
        bottomControls = new BottomControls(this, buildInfoView, () -> {
            if (!loadingCodes) { manualCheckPending = true; reloadCodesInBackground(); }
        }, this::openCloudFilePicker, () -> { if (garmin != null) garmin.sendNow(); });

        showBuildInformation();

        /*
         * Beim ersten Öffnen wird die Liste einmal geladen.
         * Alle folgenden Abfragen erfolgen direkt aus
         * dem Arbeitsspeicher.
         */
        showToday();

        StartupScreen.show(this, this::requestCloudFileAccessIfNeeded);
    }

    private void bindViews() {
        codeLabelView = findViewById(R.id.codeLabelView);
        dataStatusView = findViewById(R.id.dataStatusView);
        availabilityView = findViewById(R.id.availabilityView);
        checkStatusView = findViewById(R.id.checkStatusView);
        codeView =
                findViewById(R.id.codeView);

        dateView =
                findViewById(R.id.dateView);

        yesterdayCodeView =
                findViewById(
                        R.id.yesterdayCodeView
                );

        code2000View =
                findViewById(
                        R.id.code2000View
                );

        code2001View =
                findViewById(
                        R.id.code2001View
                );

        code2006View =
                findViewById(
                        R.id.code2006View
                );

        buildInfoView =
                findViewById(
                        R.id.buildInfoView
                );

        otherDateButton =
                findViewById(
                        R.id.otherDateButton
                );

        todayButton =
                findViewById(
                        R.id.todayButton
                );
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.add(0, 2, 0, "Jetzt prüfen");
        menu.add(0, 1, 1, "OneDrive-Datei auswählen");
        menu.add(0, 3, 2, "Garmin-Uhr");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == 3) { garmin.showMenu(); return true; }
        if (item.getItemId() == 2) {
            if (!manualCheckPending) {
                manualCheckPending = true;
                checkStatusView.setText(R.string.check_running);
                checkStatusView.setVisibility(View.VISIBLE);
                reloadCodesInBackground();
            }
            return true;
        }
        if (item.getItemId() == 1) {
            openCloudFilePicker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestCloudFileAccessIfNeeded() {
        if (!CloudCodeFileAccess.hasSavedFile(this)) openCloudFilePicker();
    }

    private void openCloudFilePicker() {
        Toast.makeText(this, "Bitte Tagescodes.txt aus OneDrive auswählen.",
                Toast.LENGTH_LONG).show();
        try {
            startActivityForResult(CloudCodeFileAccess.createFilePickerIntent(),
                    CloudCodeFileAccess.REQUEST_CLOUD_CODE_FILE);
        } catch (android.content.ActivityNotFoundException exception) {
            Toast.makeText(this, "Kein Dateiauswahldialog verfügbar.", Toast.LENGTH_LONG).show();
        }
    }

    private void configureButtons() {
        /*
         * Sowohl das Tippen auf die grosse Zahl als auch
         * der blaue Button öffnen die Datumsauswahl.
         */
        codeView.setOnClickListener(
                view -> openDatePicker()
        );

        codeView.setOnLongClickListener(view -> {
            String code = codeView.getText().toString();
            if (!code.matches("[0-9]{6}")) {
                Toast.makeText(this, R.string.no_code_to_copy, Toast.LENGTH_SHORT).show();
                return true;
            }
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Tagescode", code));
                Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        otherDateButton.setOnClickListener(
                view -> openDatePicker()
        );

        buildInfoView.setOnClickListener(view -> {
            android.widget.PopupMenu menu = new android.widget.PopupMenu(this, buildInfoView);
            onCreateOptionsMenu(menu.getMenu());
            menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
            menu.show();
        });

        todayButton.setOnClickListener(
                view -> showToday()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        garmin = new GarminConnection(this);
        garmin.setStateListener(() -> { if (bottomControls != null && garmin != null) bottomControls.watchBusy(garmin.busy()); });
    }

    @Override
    protected void onStop() {
        if (garmin != null) { garmin.close(); garmin = null; }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * onResume wird direkt nach onCreate ebenfalls
         * aufgerufen. Die Anzeige muss dabei nicht ein
         * zweites Mal vollständig aufgebaut werden.
         */
        if (firstResume) {
            firstResume = false;

            reloadCodesInBackground();
            return;
        }

        /*
         * Zuerst wird ohne Verzögerung der vorhandene
         * Cache angezeigt.
         */
        showToday();

        TagescodeWidget.updateAllWidgets(
                this
        );

        /*
         * Danach wird im Hintergrund geprüft, ob sich
         * die lokalen Codeliste geändert hat.
         */
        reloadCodesInBackground();
    }

    private void openDatePicker() {
        CodeCalendarDialog.show(this, CodeRepository.availableDates(this), displayedDate,
                date -> showDate(date, true));
    }

    private void showToday() {
        LocalDate today =
                LocalDate.now();

        showDate(
                today,
                false
        );
    }

    private void showDate(
            LocalDate date,
            boolean manuallySelected
    ) {
        /*
         * Diese Abfrage erfolgt direkt aus der Map im
         * Arbeitsspeicher und benötigt kein erneutes
         * Lesen der lokalen Codeliste.
         */
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
        codeLabelView.setText(date.equals(LocalDate.now())
                ? R.string.code_today_label : R.string.code_selected_label);
        updateDataStatus();

        dateView.setText(
                capitalise(
                        longDateFormat.format(date)
                )
        );

        if (!date.equals(LocalDate.now())) {
            todayButton.setVisibility(
                    View.VISIBLE
            );

        } else {
            todayButton.setVisibility(
                    View.GONE
            );
        }

        otherDateButton.setVisibility(
                View.VISIBLE
        );

        updateAdditionalCodes();
    }

    private void updateAdditionalCodes() {
        LocalDate yesterday =
                LocalDate.now()
                        .minusDays(1);

        String yesterdayCode =
                getCode(yesterday);

        yesterdayCodeView.setText(
                getString(
                        R.string.yesterday_code_format,
                        shortDateFormat.format(yesterday),
                        yesterdayCode
                )
        );

        LocalDate date2000 =
                LocalDate.of(
                        2000,
                        1,
                        1
                );

        code2000View.setText(
                getString(
                        R.string.fixed_code_format,
                        shortDateFormat.format(date2000),
                        getCode(date2000)
                )
        );

        LocalDate date2001 =
                LocalDate.of(
                        2001,
                        1,
                        1
                );

        code2001View.setText(
                getString(
                        R.string.fixed_code_format,
                        shortDateFormat.format(date2001),
                        getCode(date2001)
                )
        );

        LocalDate date2006 =
                LocalDate.of(
                        2006,
                        1,
                        1
                );

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

    private void updateDataStatus() {
        checkStatusView.setText(manualCheckPending ? R.string.check_running : R.string.check_failed);
        checkStatusView.setVisibility(manualCheckPending || CodeRepository.lastCheckFailed(this)
                ? View.VISIBLE : View.GONE);
        long importedAt = CodeRepository.lastSuccessfulImport(this);
        String imported = importedAt == 0 ? getString(R.string.import_unknown)
                : getString(R.string.last_import_format,
                    new java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY)
                        .format(new java.util.Date(importedAt)));
        String latest = CodeRepository.latestAvailableDate(this);
        String available = latest == null ? getString(R.string.no_codes)
                : getString(R.string.codes_until_format, shortDateFormat.format(LocalDate.parse(latest)));
        long newCodesAt = CodeRepository.lastNewCodesImport(this);
        String newCodes = newCodesAt == 0 ? getString(R.string.new_codes_import_unknown)
                : getString(R.string.new_codes_import_format,
                    new java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMANY)
                        .format(new java.util.Date(newCodesAt)));
        boolean missingToday = !CodeRepository.getCodeForToday(this).matches("[0-9]{6}");
        availabilityView.setText(missingToday
                ? getString(R.string.missing_today) + "\n" + available : available);
        availabilityView.setBackgroundColor(missingToday ? 0xFFFFCC00 : android.graphics.Color.TRANSPARENT);
        availabilityView.setTextColor(missingToday ? android.graphics.Color.BLACK : getColor(R.color.text_primary));
        dataStatusView.setText(newCodes + "\n" + imported);
        if (bottomControls != null) bottomControls.update(loadingCodes);
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

    private void reloadCodesInBackground() {
        if (loadingCodes) return;
        loadingCodes = true;
        if (bottomControls != null) bottomControls.update(true);
        int currentRequest =
                reloadRequestNumber
                        .incrementAndGet();

        codeExecutor.execute(() -> {
            /*
             * Das Einlesen und Analysieren der Datei
             * erfolgt nicht auf dem UI-Thread.
             */
            CodeRepository.reload(
                    getApplicationContext()
            );

            int imported = CodeRepository.importCloudCodes(getApplicationContext(), () -> mainHandler.post(() -> {
                if (!isDestroyed() && bottomControls != null) bottomControls.importing();
            }));
            mainHandler.post(() -> {
                if (
                        isFinishing()
                                || isDestroyed()
                ) {
                    return;
                }

                /*
                 * Falls zwischenzeitlich eine neuere
                 * Aktualisierung angefordert wurde,
                 * muss diese Anzeige nicht mehr
                 * aktualisiert werden.
                 */
                if (
                        currentRequest
                                != reloadRequestNumber.get()
                ) {
                    return;
                }

                loadingCodes = false;
                boolean wasManualCheck = manualCheckPending;
                manualCheckPending = false;
                if (wasManualCheck && imported == 0) {
                    Toast.makeText(this, R.string.check_up_to_date, Toast.LENGTH_SHORT).show();
                }
                if (imported < 0) {
                    Toast.makeText(this, "OneDrive-Datei nicht lesbar. Gespeicherte Codes bleiben verfügbar. "
                            + "Zum erneuten Auswählen unten auf die Version tippen.", Toast.LENGTH_LONG).show();
                } else if (imported > 0) {
                    Toast.makeText(this, imported + " neue Tagescodes gespeichert.", Toast.LENGTH_SHORT).show();
                }
                showDate(manuallySelectedDate ? displayedDate : LocalDate.now(), manuallySelectedDate);

                TagescodeWidget.updateAllWidgets(
                        this
                );
                if (garmin != null) garmin.sync();
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

        if (requestCode == CloudCodeFileAccess.REQUEST_CLOUD_CODE_FILE) {
            if (resultCode == RESULT_OK) {
                if (CloudCodeFileAccess.saveFileAccess(this, data)) {
                    reloadCodesInBackground();
                } else {
                    Toast.makeText(this, "Dauerhafter Dateizugriff konnte nicht gespeichert werden.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Später auswählen: unten auf die Version tippen.",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }

    }

    @Override
    protected void onDestroy() {
        /*
         * Ausstehende Hintergrundaufgaben werden beim
         * vollständigen Schliessen der Activity beendet.
         */
        codeExecutor.shutdownNow();
        if (garmin != null) garmin.close();

        super.onDestroy();
    }
}
