package ch.florian.tagescode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import java.util.Calendar;

public class TagescodeWidget extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "ch.florian.tagescode.REFRESH";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleNextMidnight(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelMidnightAlarm(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
        scheduleNextMidnight(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || ACTION_REFRESH.equals(action)) {
            updateAllWidgets(context);
            scheduleNextMidnight(context);
        }
    }

    static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TagescodeWidget.class));
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tagescode);
        views.setTextViewText(R.id.widgetCode, CodeRepository.getCodeForToday(context));

        Intent openDateSearch = new Intent(context, MainActivity.class);
        openDateSearch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openDateSearch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        manager.updateAppWidget(id, views);
    }

    private static void scheduleNextMidnight(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 2);
        nextMidnight.set(Calendar.MILLISECOND, 0);

        PendingIntent pendingIntent = refreshPendingIntent(context);
        alarmManager.cancel(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMidnight.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextMidnight.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private static void cancelMidnightAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(refreshPendingIntent(context));
        }
    }

    private static PendingIntent refreshPendingIntent(Context context) {
        Intent intent = new Intent(context, TagescodeWidget.class);
        intent.setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
