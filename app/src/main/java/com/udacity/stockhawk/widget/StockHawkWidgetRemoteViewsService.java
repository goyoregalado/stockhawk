package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by goyo on 14/5/17.
 */

public class StockHawkWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockHawkWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
