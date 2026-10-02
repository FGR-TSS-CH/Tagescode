package ch.florian.tagescode;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** Shares refresh events and the daily schedule with the regular widget. */
public final class CompactTagescodeWidget extends TagescodeWidget {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateCompactWidgets(context);
        DailyWidgetUpdateReceiver.scheduleNextUpdate(context);
    }

    static void updateCompactWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, CompactTagescodeWidget.class));
        if (ids.length == 0) return;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tagescode_widget_compact);
        views.setTextViewText(R.id.widgetCodeView, CodeRepository.getCodeForToday(context));
        Intent open = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        views.setOnClickPendingIntent(R.id.widgetRoot, PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        manager.updateAppWidget(ids, views);
    }
}
