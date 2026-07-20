package ch.florian.tagescode;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
            new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.codeView);
        dateView = findViewById(R.id.dateView);
        otherDateButton = findViewById(R.id.otherDateButton);
        todayButton = findViewById(R.id.todayButton);

        otherDateButton.setOnClickListener(view -> openDatePicker());
        todayButton.setOnClickListener(view -> showToday());

        showToday();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showToday();
    }

    @Override
    protected void onPause() {
        showToday();
        TagescodeWidget.updateAllWidgets(this);
        super.onPause();
    }

    private void openDatePicker() {
        Calendar today = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        showSelectedDate(year, month, dayOfMonth),
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showToday() {
        Calendar today = Calendar.getInstance();

        codeView.setText(CodeRepository.getCodeForToday(this));
        dateView.setText(capitalise(dateFormat.format(today.getTime())));

        todayButton.setVisibility(View.GONE);
        otherDateButton.setVisibility(View.VISIBLE);
    }

    private void showSelectedDate(int year, int month, int dayOfMonth) {
        Calendar selected = Calendar.getInstance();
        selected.clear();
        selected.set(year, month, dayOfMonth);

        codeView.setText(
                CodeRepository.getCodeForDate(this, year, month, dayOfMonth)
        );

        dateView.setText(capitalise(dateFormat.format(selected.getTime())));

        otherDateButton.setVisibility(View.VISIBLE);
        todayButton.setVisibility(View.VISIBLE);
    }

    private String capitalise(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase(Locale.GERMANY)
                + text.substring(1);
    }
}
