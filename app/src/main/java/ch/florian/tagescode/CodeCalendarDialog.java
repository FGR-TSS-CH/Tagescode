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
    private final android.app.Dialog dialog;
    private int monthIndex;

    static void show(Context context, AvailableCodeDates dates, LocalDate preferred,
                     Consumer<LocalDate> onSelected) {
        if (dates.months().isEmpty()) {
            new AlertDialog.Builder(context).setTitle("Datum auswählen")
                    .setMessage("Es sind noch keine Tagescodes verfügbar.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        new CodeCalendarDialog(context, dates, preferred, onSelected).open();
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
        dialog = new android.app.Dialog(context);dialog.requestWindowFeature(1);
        LinearLayout shell=new LinearLayout(context);shell.setOrientation(1);shell.setPadding(dp(16),dp(16),dp(16),dp(16));shell.setBackground(background(dark()?0xFF10181E:0xFFFFFFFF,20));
        LinearLayout header=new LinearLayout(context);header.setGravity(16);TextView heading=new TextView(context);heading.setText("Datum auswählen");heading.setTextSize(20);heading.setTypeface(null,1);header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));Button close=button("Schliessen");close.setTextSize(11);close.setOnClickListener(v->dialog.dismiss());header.addView(close,new LinearLayout.LayoutParams(dp(76),dp(44)));shell.addView(header);shell.addView(scroll);dialog.setContentView(shell);
        render();
    }

    private boolean dark(){return (context.getResources().getConfiguration().uiMode & 48)==32;}
    private android.graphics.drawable.GradientDrawable background(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private void open(){dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));dialog.getWindow().setLayout(Math.min(context.getResources().getDisplayMetrics().widthPixels-dp(32),dp(460)),-2);}
    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private Button button(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextSize(14);button.setAllCaps(false);button.setStateListAnimator(null);button.setBackgroundTintList(null);button.setBackground(background(0,10));button.setTextColor(dark()?0xFFEDF2F6:0xFF161A1E);
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
            if(available)day.setBackground(background(dark()?0xFF202D37:0xFFEAF0F4,10));
            if(date.equals(preferred)&&available){day.setTypeface(null,android.graphics.Typeface.BOLD);day.setBackground(background(0xFF005D9C,10));day.setTextColor(0xFFFFFFFF);}
            if (available) {
                day.setOnClickListener(v -> {
                    onSelected.accept(date);
                    dialog.dismiss();
                });
            }
            LinearLayout.LayoutParams cellParams=new LinearLayout.LayoutParams(0,dp(44),1);cellParams.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(day,cellParams);
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
