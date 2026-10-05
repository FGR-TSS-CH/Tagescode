package ch.florian.tagescode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
final class ManualCodeDate {
    private static final DateTimeFormatter FORMAT=DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    static LocalDate parse(String text){return LocalDate.parse(text.trim(),FORMAT);}
}
