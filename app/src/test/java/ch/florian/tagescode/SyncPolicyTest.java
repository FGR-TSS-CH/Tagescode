package ch.florian.tagescode;
import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;
public class SyncPolicyTest {
 @Test public void mondayMorningRetriesEvenWithOldSuccessfulFile(){assertTrue(SyncPolicy.due(LocalDateTime.of(2026,11,9,8,30),1800000,false,false));}
 @Test public void normalDayDoesNotPollEveryHalfHour(){assertFalse(SyncPolicy.due(LocalDateTime.of(2026,11,10,8,30),1800000,false,false));}
 @Test public void missingCodesAndFailuresRetry(){assertTrue(SyncPolicy.due(LocalDateTime.of(2026,11,10,8,30),1800000,true,false));assertTrue(SyncPolicy.due(LocalDateTime.of(2026,11,10,8,30),1800000,false,true));}
 @Test public void recentCheckWaits(){assertFalse(SyncPolicy.due(LocalDateTime.of(2026,11,9,8,30),60000,true,true));}
}
