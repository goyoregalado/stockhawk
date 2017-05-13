package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.udacity.stockhawk.R;

/**
 * Created by goyo on 13/5/17.
 */

public class StockHawkBroadcastReceiver extends BroadcastReceiver {

    public  static final String BAD_SYMBOL = "com.udacity.stockhawk.BAD_SYMBOL";
    public static final String BAD_SYMBOL_KEY = "data";

    @Override
    public void onReceive(Context context, Intent intent) {
        String symbol = intent.getStringExtra(BAD_SYMBOL_KEY);
        String error_msg = context.getString(R.string.toast_invalid_stock_error) + " " + symbol;
        Toast toast = Toast.makeText(context, error_msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
