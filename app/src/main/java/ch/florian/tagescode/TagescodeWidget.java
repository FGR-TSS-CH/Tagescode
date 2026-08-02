package ch.florian.tagescode;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TagescodeWidget extends AppWidgetProvider {

    private static final String ACTION_REFRESH =
            "ch.florian.tagescode.REFRESH";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy",
                    Locale.GERMANY
            );

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds
    ) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
            );
        }
    }

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        super.onReceive(context, intent);

        if (intent == null) {
            return;
        }

        String action = intent.getAction();

        if (
                Intent.ACTION_DATE_CHANGED.equals(action)
                        || Intent.ACTION_TIME_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                        || Intent.ACTION_BOOT_COMPLETED.equals(action)
                        || ACTION_REFRESH.equals(action)
        ) {
            updateAllWidgets(context);
        }
    }

    static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager =
                AppWidgetManager.getInstance(context);

        ComponentName widgetComponent =
                new ComponentName(
                        context,
                        TagescodeWidget.class
                );

        int[] appWidgetIds =
                appWidgetManager.getAppWidgetIds(
                        widgetComponent
                );

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
            );
        }
    }

    private static void updateAppWidget(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId
    ) {
        LocalDate today =
                LocalDate.now();

        String currentCode =
                CodeRepository.getCodeForDate(
                        context,
                        today.getYear(),
                        today.getMonthValue() - 1,
                        today.getDayOfMonth()
                );

        String currentDate =
                DATE_FORMAT.format(today);

        RemoteViews views =
                new RemoteViews(
                        context.getPackageName(),
                        R.layout.tagescode_widget
                );

        views.setTextViewText(
                R.id.widgetCodeView,
                currentCode
        );

        views.setTextViewText(
                R.id.widgetDateView,
                currentDate
        );

        Intent openAppIntent =
                new Intent(
                        context,
                        MainActivity.class
                );

        openAppIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        views.setOnClickPendingIntent(
                R.id.widgetRoot,
                openAppPendingIntent
        );

        appWidgetManager.updateAppWidget(
                appWidgetId,
                views
        );
    }
}
