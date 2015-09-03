package com.jeonsoft.facebundypro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by WendellWayne on 5/26/2015.
 */
public class FaceBundyAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int i=0; i<appWidgetIds.length; i++){
            int currentWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, FaceBundyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.app_widget_layout);
            views.setOnClickPendingIntent(R.id.llWidget, pending);
            appWidgetManager.updateAppWidget(currentWidgetId, views);
        }
    }
}
