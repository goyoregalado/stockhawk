package com.udacity.stockhawk;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.util.ArrayList;

/**
 * Created by goyo on 13/5/17.
 */

public class StockHawkWidgetIntentService extends IntentService{

    StockHawkWidgetIntentService() {
        super("StockHawkIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int []appWidgetsIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockHawkWidget.class));


        // Query the data...
        for (int appWidgetId: appWidgetsIds) {
            int layoutId = R.layout.stock_hawk_widget;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            // Here we will load the views with the data.
            // This should be a thread.
            Cursor cursor = this.getContentResolver().query(
                    Contract.Quote.URI,
                    (String []) Contract.Quote.QUOTE_COLUMNS.toArray(),
                    null,
                    null,
                    null
            );

            // An intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.stockhawk_widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }
}
