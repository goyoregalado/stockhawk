package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.MoneyFormatUtils;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

/**
 * Created by goyo on 15/5/17.
 */

public class StockHawkWidgetRemoteViewsFactory implements StockHawkWidgetRemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor data;

    public StockHawkWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        Timber.d("Ok, dataset change, querying");
        Uri stocksUri = Contract.Quote.URI;
        data = mContext.getContentResolver().query(stocksUri, null, null, null, null);

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);


        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        float price = data.getFloat(Contract.Quote.POSITION_PRICE);
        float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        String priceStr = MoneyFormatUtils.formatDollarWithSign(price);
        String absoluteChangeStr = MoneyFormatUtils.formatDollarWithSign(absoluteChange);
        String percentageChangeStr = MoneyFormatUtils.formatPercentage(percentageChange);


        views.setTextViewText(R.id.tv_symbol, symbol);
        views.setTextViewText(R.id.tv_price, priceStr);
        if(PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.tv_change, absoluteChangeStr);
        }
        else {
            views.setTextViewText(R.id.tv_change, percentageChangeStr);
        }

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(MainActivity.STOCK_KEY, symbol);
        fillInIntent.putExtra(MainActivity.DISPLAY_MODE_KEY, PrefUtils.getDisplayMode(mContext));
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position)) {
            return data.getLong(Contract.Quote.POSITION_ID);
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
