package ch.florian.tagescode;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
final class SyncPolicy {
 static boolean due(LocalDateTime now,long elapsed,boolean missing,boolean failed){
  boolean monday=now.getDayOfWeek()==DayOfWeek.MONDAY && now.getHour()>=6 && now.getHour()<12;
  long interval=(monday || missing || failed)?30*60*1000L:6*60*60*1000L;
  return elapsed<0 || elapsed>=interval;
 }
}
