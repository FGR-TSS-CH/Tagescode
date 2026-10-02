package ch.florian.tagescode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class DailyWidgetUpdateReceiver
        extends BroadcastReceiver {

    private static final int REQUEST_CODE =
            1001;

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        // Draw from local storage immediately; sync cloud data in a scheduled job.
        CodeSyncJobService.enqueue(context);
        TagescodeWidget.updateAllWidgets(
                context
        );

        /*
         * Anschliessend direkt den Alarm für den
         * nächsten Tag setzen.
         */
        scheduleNextUpdate(
                context
        );
    }

    public static void scheduleNextUpdate(
            Context context
    ) {
        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager == null) {
            return;
        }

        /*
         * Nächster Tag um 00:01 Uhr.
         */
        Calendar nextUpdate =
                Calendar.getInstance();

        nextUpdate.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        nextUpdate.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        nextUpdate.set(
                Calendar.MINUTE,
                1
        );

        nextUpdate.set(
                Calendar.SECOND,
                0
        );

        nextUpdate.set(
                Calendar.MILLISECOND,
                0
        );

        Intent updateIntent =
                new Intent(
                        context,
                        DailyWidgetUpdateReceiver.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE,
                        updateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        /*
         * setAndAllowWhileIdle:
         *
         * Funktioniert auch bei Doze / Energiesparmodus,
         * ohne dass die App die spezielle Berechtigung
         * für exakte Alarme benötigt.
         */
        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextUpdate.getTimeInMillis(),
                pendingIntent
        );
    }
}
