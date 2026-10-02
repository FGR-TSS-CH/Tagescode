package ch.florian.tagescode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/** A snapshot of dates that can actually be selected, including gaps. */
final class AvailableCodeDates {
    private final NavigableSet<LocalDate> dates = new TreeSet<>();
    private final List<YearMonth> months;

    AvailableCodeDates(Collection<String> isoDates) {
        TreeSet<YearMonth> availableMonths = new TreeSet<>();
        for (String value : isoDates) {
            LocalDate date = LocalDate.parse(value);
            dates.add(date);
            availableMonths.add(YearMonth.from(date));
        }
        months = Collections.unmodifiableList(new ArrayList<>(availableMonths));
    }

    boolean contains(LocalDate date) { return dates.contains(date); }
    List<YearMonth> months() { return months; }

    int initialMonth(LocalDate preferred) {
        if (dates.isEmpty()) return -1;
        LocalDate date = dates.floor(preferred);
        if (date == null) date = dates.first();
        return months.indexOf(YearMonth.from(date));
    }
}
