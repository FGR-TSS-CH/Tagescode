package ch.florian.tagescode;

import android.app.Activity;
import android.app.DatePickerDialog;
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
     * Das erneute Einlesen der PwD.txt erfolgt in einem
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

    private boolean firstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        bindViews();
        configureButtons();

        showBuildInformation();

        /*
         * Beim ersten Öffnen wird die Liste einmal geladen.
         * Alle folgenden Abfragen erfolgen direkt aus
         * dem Arbeitsspeicher.
         */
        showToday();

        requestCodeFolderAccessIfNeeded();
    }

    private void bindViews() {
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

    private void configureButtons() {
        /*
         * Sowohl das Tippen auf die grosse Zahl als auch
         * der blaue Button öffnen die Datumsauswahl.
         */
        codeView.setOnClickListener(
                view -> openDatePicker()
        );

        otherDateButton.setOnClickListener(
                view -> openDatePicker()
        );

        todayButton.setOnClickListener(
                view -> showToday()
        );
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
         * die PwD.txt geändert hat.
         */
        reloadCodesInBackground();
    }

    private void openDatePicker() {
        LocalDate today =
                LocalDate.now();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) ->
                                showSelectedDate(
                                        year,
                                        month,
                                        dayOfMonth
                                ),
                        today.getYear(),
                        today.getMonthValue() - 1,
                        today.getDayOfMonth()
                );

        dialog.setOnCancelListener(
                ignored -> showToday()
        );

        dialog.show();
    }

    private void showToday() {
        LocalDate today =
                LocalDate.now();

        showDate(
                today,
                false
        );
    }

    private void showSelectedDate(
            int year,
            int zeroBasedMonth,
            int dayOfMonth
    ) {
        try {
            LocalDate selectedDate =
                    LocalDate.of(
                            year,
                            zeroBasedMonth + 1,
                            dayOfMonth
                    );

            showDate(
                    selectedDate,
                    true
            );

        } catch (Exception ignored) {
            showToday();
        }
    }

    private void showDate(
            LocalDate date,
            boolean manuallySelected
    ) {
        /*
         * Diese Abfrage erfolgt direkt aus der Map im
         * Arbeitsspeicher und benötigt kein erneutes
         * Lesen der PwD.txt.
         */
        String code =
                CodeRepository.getCodeForDate(
                        this,
                        date.getYear(),
                        date.getMonthValue() - 1,
                        date.getDayOfMonth()
                );

        codeView.setText(code);

        dateView.setText(
                capitalise(
                        longDateFormat.format(date)
                )
        );

        if (manuallySelected) {
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

                showToday();

                TagescodeWidget.updateAllWidgets(
                        this
                );
            });
        });
    }

    private void requestCodeFolderAccessIfNeeded() {
        if (
                CodeFolderAccess.hasSavedFolder(
                        this
                )
        ) {
            return;
        }

        Toast.makeText(
                this,
                "Bitte einmalig den Ordner "
                        + "DCIM/Videojet/PwD auswählen.",
                Toast.LENGTH_LONG
        ).show();

        startActivityForResult(
                CodeFolderAccess
                        .createFolderPickerIntent(),
                CodeFolderAccess
                        .REQUEST_CODE_FOLDER
        );
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
                        != CodeFolderAccess
                        .REQUEST_CODE_FOLDER
        ) {
            return;
        }

        if (resultCode != RESULT_OK) {
            Toast.makeText(
                    this,
                    "Der Ordner wurde nicht freigegeben.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        boolean saved =
                CodeFolderAccess.saveFolderAccess(
                        this,
                        data
                );

        if (saved) {
            /*
             * Der alte Cache wird verworfen und die neu
             * freigegebene PwD.txt im Hintergrund geladen.
             */
            CodeRepository.invalidate();
            reloadCodesInBackground();

            Toast.makeText(
                    this,
                    "PwD-Ordner wurde gespeichert.",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Toast.makeText(
                    this,
                    "Der Ordnerzugriff konnte "
                            + "nicht gespeichert werden.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        /*
         * Ausstehende Hintergrundaufgaben werden beim
         * vollständigen Schliessen der Activity beendet.
         */
        codeExecutor.shutdownNow();

        super.onDestroy();
    }
}
