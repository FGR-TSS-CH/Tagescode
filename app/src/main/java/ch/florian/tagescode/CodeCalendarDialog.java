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
        Button monthButton=button(month.format(DateTimeFormatter.ofPattern("MMMM",Locale.GERMAN)));
        monthButton.setContentDescription("Monat auswählen");monthButton.setOnClickListener(v->chooseMonth());
        navigation.addView(monthButton,new LinearLayout.LayoutParams(0,dp(48),1));
        Button yearButton=button(Integer.toString(month.getYear()));
        yearButton.setContentDescription("Jahr auswählen");yearButton.setOnClickListener(v->chooseYear());
        navigation.addView(yearButton,new LinearLayout.LayoutParams(dp(70),dp(48)));
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
        Button manual=button("Datum manuell eingeben");
        manual.setTextColor(0xFF579FCB);
        manual.setOnClickListener(v -> showManualInput());
        calendar.addView(manual,new LinearLayout.LayoutParams(-1,dp(48)));
    }

    private void showManualInput() {
        calendar.removeAllViews();
        TextView label=new TextView(context);label.setText("Datum eingeben (TT.MM.JJJJ)");calendar.addView(label);
        android.widget.EditText input=new android.widget.EditText(context);
        input.setSingleLine(true);input.setHint("TT.MM.JJJJ");input.setTextSize(20);
        input.setInputType(android.text.InputType.TYPE_CLASS_DATETIME | android.text.InputType.TYPE_DATETIME_VARIATION_DATE);
        input.setText(preferred.format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));input.selectAll();
        input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        calendar.addView(input,new LinearLayout.LayoutParams(-1,dp(56)));
        Runnable submit=()->{
            LocalDate date;
            try { date=ManualCodeDate.parse(input.getText().toString()); }
            catch(java.time.format.DateTimeParseException e){input.setError("Bitte ein gültiges Datum als TT.MM.JJJJ eingeben.");return;}
            if(!dates.contains(date)){input.setError("Für dieses Datum ist kein Tagescode verfügbar.");return;}
            ((android.view.inputmethod.InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
            onSelected.accept(date);dialog.dismiss();
        };
        Button select=button("Datum anzeigen");select.setBackground(background(0xFF005D9C,10));select.setTextColor(0xFFFFFFFF);select.setOnClickListener(v->submit.run());calendar.addView(select,new LinearLayout.LayoutParams(-1,dp(48)));
        Button back=button("Zurück zum Kalender");back.setOnClickListener(v->{((android.view.inputmethod.InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);render();});calendar.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));
        input.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){submit.run();return true;}return false;});
        input.requestFocus();input.post(()->((android.view.inputmethod.InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT));
    }

    private void chooseYear() {
        YearMonth current=dates.months().get(monthIndex);
        java.util.List<Integer> years=new java.util.ArrayList<>();
        for(YearMonth month:dates.months())if(!years.contains(month.getYear()))years.add(month.getYear());
        String[] labels=years.stream().map(String::valueOf).toArray(String[]::new);
        choose("Jahr auswählen",labels,years.indexOf(current.getYear()),index->{
            int year=years.get(index);
            YearMonth target=YearMonth.of(year,current.getMonthValue());
            int found=dates.months().indexOf(target);
            if(found<0)for(int i=0;i<dates.months().size();i++)if(dates.months().get(i).getYear()==year){found=i;break;}
            monthIndex=found;render();
        });
    }
    private void chooseMonth() {
        YearMonth current=dates.months().get(monthIndex);
        java.util.List<YearMonth> months=new java.util.ArrayList<>();
        for(YearMonth month:dates.months())if(month.getYear()==current.getYear())months.add(month);
        DateTimeFormatter name=DateTimeFormatter.ofPattern("MMMM",Locale.GERMAN);
        String[] labels=months.stream().map(name::format).toArray(String[]::new);
        choose("Monat auswählen",labels,months.indexOf(current),index->{monthIndex=dates.months().indexOf(months.get(index));render();});
    }
    private void choose(String title,String[] labels,int selected,java.util.function.IntConsumer accept) {
        android.app.Dialog picker=new android.app.Dialog(context);picker.requestWindowFeature(1);
        LinearLayout panel=new LinearLayout(context);panel.setOrientation(1);panel.setPadding(dp(16),dp(12),dp(16),dp(12));panel.setBackground(background(dark()?0xFF10181E:0xFFFFFFFF,20));
        TextView heading=new TextView(context);heading.setText(title);heading.setTextSize(20);heading.setPadding(0,0,0,dp(12));panel.addView(heading);
        android.widget.ListView list=new android.widget.ListView(context);
        list.setAdapter(new android.widget.ArrayAdapter<String>(context,android.R.layout.simple_list_item_single_choice,labels));list.setChoiceMode(android.widget.ListView.CHOICE_MODE_SINGLE);list.setItemChecked(selected,true);list.setSelection(selected);
        panel.addView(list,new LinearLayout.LayoutParams(-1,Math.min(dp(360),labels.length*dp(48))));
        list.setOnItemClickListener((parent,view,position,id)->{picker.dismiss();accept.accept(position);});
        Button cancel=button("Abbrechen");cancel.setOnClickListener(v->picker.dismiss());panel.addView(cancel);picker.setContentView(panel);picker.show();picker.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));picker.getWindow().setLayout(Math.min(context.getResources().getDisplayMetrics().widthPixels-dp(48),dp(400)),-2);
    }
}
