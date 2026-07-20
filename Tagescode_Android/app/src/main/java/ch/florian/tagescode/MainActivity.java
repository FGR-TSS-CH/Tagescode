package ch.florian.tagescode;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends Activity {
    private TextView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.selectedCode);
        DatePicker datePicker = findViewById(R.id.datePicker);
        Button closeButton = findViewById(R.id.closeButton);

        Calendar today = Calendar.getInstance();
        datePicker.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) ->
                        showCodeForDate(year, monthOfYear, dayOfMonth)
        );

        showCodeForDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        closeButton.setOnClickListener(v -> finish());
    }

    private void showCodeForDate(int year, int month, int dayOfMonth) {
        codeView.setText(CodeRepository.getCodeForDate(this, year, month, dayOfMonth));
    }

    @Override
    protected void onStop() {
        super.onStop();
        TagescodeWidget.updateAllWidgets(this);
    }
}
