package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.MoneyFormatUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by goyo on 14/5/17.
 */

public class DetailActivity extends AppCompatActivity {

    private Context mContext;

    @BindView(R.id.tv_symbol)
    public TextView tvSymbol;

    @BindView(R.id.tv_price)
    public TextView tvPrice;

    @BindView(R.id.tv_change)
    public TextView tvChange;



    @BindView(R.id.chart)
    public LineChart chart;

    @Override
    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);


        mContext = this;

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intentThatStartedActivity = getIntent();
        if (intentThatStartedActivity.hasExtra(MainActivity.STOCK_KEY)
                && intentThatStartedActivity.hasExtra(MainActivity.DISPLAY_MODE_KEY)) {
            String symbol = intentThatStartedActivity.getStringExtra(MainActivity.STOCK_KEY);
            tvSymbol.setText(symbol);
            String displayMode = intentThatStartedActivity.getStringExtra(MainActivity.DISPLAY_MODE_KEY);
            Cursor cursor = getContentResolver().query(
                    Contract.Quote.makeUriForStock(symbol),
                    null,
                    null,
                    null,
                    null);
            try {
                while (cursor.moveToNext()) {

                    String price = MoneyFormatUtils.formatDollar(cursor.getFloat(Contract.Quote.POSITION_PRICE));
                    tvPrice.setText(price);


                    float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                    String change = MoneyFormatUtils.formatDollarWithSign(rawAbsoluteChange);
                    String percentage = MoneyFormatUtils.formatPercentage(percentageChange / 100);

                    if (displayMode.equals(this.getString(R.string.pref_display_mode_absolute_key))) {
                        if (rawAbsoluteChange > 0) {
                            tvChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                        } else {
                            tvChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                        }
                        tvChange.setText(change);
                    }
                    else {
                        if (percentageChange > 0) {
                            tvChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                        } else {
                            tvChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                        }
                        tvChange.setText(percentage);
                    }
                    String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
                    historyParser(history);
                }
            }
            finally {
                cursor.close();
            }
        }
    }

    private void historyParser(String history) {
        String[] historyArray = null;
        ArrayList<Entry> entryArray = new ArrayList<>();

        historyArray = history.split("\n");

        for(int i = 0; i < historyArray.length; i++) {
            String value = historyArray[i];
            String[] records = value.split(",");
            long timestamp = Long.parseLong(records[0]);
            float price = Float.parseFloat(records[1]);
            Entry entry = new Entry(timestamp, price);
            entryArray.add(entry);
        }

        // We sort the entries by timestamp;
        Collections.sort(entryArray, new EntryXComparator());
        LineDataSet dataset = new LineDataSet(entryArray, tvSymbol.getText().toString());
        dataset.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        dataset.setValueTextColor(ContextCompat.getColor(mContext, R.color.white));

        LineData lineData = new LineData(dataset);
        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new StockHawkXAxisValueFormatter());

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setValueFormatter(new StockHawkYAxisValueFormatter());

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setTextSize(10f);
        yAxisRight.setTextColor(Color.WHITE);
        yAxisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisRight.setValueFormatter(new StockHawkYAxisValueFormatter());

        Legend legend = chart.getLegend();
        legend.setTextSize(10f);
        legend.setTextColor(Color.WHITE);


        chart.invalidate();

    }

    class StockHawkXAxisValueFormatter implements IAxisValueFormatter {

        public StockHawkXAxisValueFormatter() {

        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return dateFromMilliseconds((long) value);
        }



        public String dateFromMilliseconds(long timestamp) {
            SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(
                    DateFormat.SHORT,
                    Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            String date = dateFormat.format(calendar.getTime());
            return date;
        }
    }

    class StockHawkYAxisValueFormatter implements IAxisValueFormatter {

        public StockHawkYAxisValueFormatter() {

        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return MoneyFormatUtils.formatDollarWithSign(value);
        }
    }
}
