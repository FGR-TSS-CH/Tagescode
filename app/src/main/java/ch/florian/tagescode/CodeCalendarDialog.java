package ch.florian.tagescode;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/** Calendar with disabled cells for every date without a code. */
final class CodeCalendarDialog {
    private static final DateTimeFormatter MONTH =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN);
    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN);
    private final Context context;
    private final AvailableCodeDates dates;
    private final Consumer<LocalDate> onSelected;
    private final LocalDate preferred;
    private final LinearLayout calendar;
    private final AlertDialog dialog;
    private int monthIndex;

    static void show(Context context, AvailableCodeDates dates, LocalDate preferred,
                     Consumer<LocalDate> onSelected) {
        if (dates.months().isEmpty()) {
            new AlertDialog.Builder(context).setTitle("Datum auswählen")
                    .setMessage("Es sind noch keine Tagescodes verfügbar.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        new CodeCalendarDialog(context, dates, preferred, onSelected).dialog.show();
    }

    private CodeCalendarDialog(Context context, AvailableCodeDates dates, LocalDate preferred,
                               Consumer<LocalDate> onSelected) {
        this.context = context;
        this.dates = dates;
        this.preferred = preferred;
        this.onSelected = onSelected;
        monthIndex = dates.initialMonth(preferred);
        calendar = new LinearLayout(context);
        calendar.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(8);
        calendar.setPadding(padding, padding, padding, padding);
        ScrollView scroll = new ScrollView(context);
        scroll.addView(calendar);
        dialog = new AlertDialog.Builder(context).setTitle("Datum auswählen")
                .setView(scroll).setNegativeButton("Abbrechen", null).create();
        render();
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private Button button(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextSize(14);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private void render() {
        calendar.removeAllViews();
        YearMonth month = dates.months().get(monthIndex);
        LinearLayout navigation = new LinearLayout(context);
        Button previous = button("‹");
        previous.setContentDescription("Vorheriger Monat mit Tagescodes");
        previous.setEnabled(monthIndex > 0);
        previous.setOnClickListener(v -> { monthIndex--; render(); });
        navigation.addView(previous, new LinearLayout.LayoutParams(dp(40), dp(48)));
        Button title = button(MONTH.format(month));
        title.setContentDescription(MONTH.format(month) + ", Monat und Jahr auswählen");
        title.setOnClickListener(v -> chooseMonth());
        navigation.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button next = button("›");
        next.setContentDescription("Nächster Monat mit Tagescodes");
        next.setEnabled(monthIndex < dates.months().size() - 1);
        next.setOnClickListener(v -> { monthIndex++; render(); });
        navigation.addView(next, new LinearLayout.LayoutParams(dp(40), dp(48)));
        calendar.addView(navigation);

        LinearLayout weekdays = new LinearLayout(context);
        for (String name : new String[]{"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"}) {
            TextView label = new TextView(context);
            label.setText(name);
            label.setGravity(Gravity.CENTER);
            weekdays.addView(label, new LinearLayout.LayoutParams(0, dp(32), 1));
        }
        calendar.addView(weekdays);
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
        int cells = ((offset + month.lengthOfMonth() + 6) / 7) * 7;
        LinearLayout row = null;
        for (int cell = 0; cell < cells; cell++) {
            if (cell % 7 == 0) {
                row = new LinearLayout(context);
                calendar.addView(row);
            }
            int number = cell - offset + 1;
            if (number < 1 || number > month.lengthOfMonth()) {
                row.addView(new TextView(context), new LinearLayout.LayoutParams(0, dp(48), 1));
                continue;
            }
            LocalDate date = month.atDay(number);
            Button day = button(Integer.toString(number));
            boolean available = dates.contains(date);
            day.setEnabled(available);
            day.setAlpha(available ? 1f : 0.3f);
            day.setContentDescription(DAY.format(date)
                    + (available ? ", Tagescode verfügbar" : ", kein Tagescode"));
            if (date.equals(preferred)) day.setTypeface(null, android.graphics.Typeface.BOLD);
            if (available) {
                day.setOnClickListener(v -> {
                    onSelected.accept(date);
                    dialog.dismiss();
                });
            }
            row.addView(day, new LinearLayout.LayoutParams(0, dp(48), 1));
        }
        TextView hint = new TextView(context);
        hint.setText("Nur Tage mit Tagescode sind auswählbar.");
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(8), 0, 0);
        calendar.addView(hint);
    }

    private void chooseMonth() {
        String[] labels = dates.months().stream().map(MONTH::format).toArray(String[]::new);
        new AlertDialog.Builder(context).setTitle("Monat und Jahr auswählen")
                .setSingleChoiceItems(labels, monthIndex, (picker, index) -> {
                    monthIndex = index;
                    picker.dismiss();
                    render();
                }).setNegativeButton("Abbrechen", null).show();
    }
}
