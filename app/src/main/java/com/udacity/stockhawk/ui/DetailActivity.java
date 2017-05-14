package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
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

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;
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

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");



        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intentThatStartedActivity = getIntent();
        if (intentThatStartedActivity.hasExtra(Intent.EXTRA_TEXT)) {
            String symbol = intentThatStartedActivity.getStringExtra(Intent.EXTRA_TEXT);
            tvSymbol.setText(symbol);
            Cursor cursor = getContentResolver().query(
                    Contract.Quote.makeUriForStock(symbol),
                    null,
                    null,
                    null,
                    null);
            try {
                while (cursor.moveToNext()) {

                            //String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL));
                    String price = dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));
                    tvPrice.setText(price);


                    float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);


                    if (rawAbsoluteChange > 0) {
                        tvChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                    } else {
                        tvChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                    }

                    String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                    String percentage = percentageFormat.format(percentageChange / 100);

                    tvChange.setText(change);
                    String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
                    Timber.d("Super history: " + history);
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
            //String x = dateFromMilliseconds(timestamp);
            Entry entry = new Entry(timestamp, price);
            entryArray.add(entry);
        }
        // We sort the entries by timestamp;
        Collections.sort(entryArray, new EntryXComparator());
        LineDataSet dataset = new LineDataSet(entryArray, tvSymbol.getText().toString());
        dataset.setColor(mContext.getColor(R.color.colorAccent));
        dataset.setValueTextColor(mContext.getColor(R.color.colorAccent));

        LineData lineData = new LineData(dataset);
        chart.setData(lineData);
 /*       Description description = new Description();
        description.setTextColor(mContext.getColor(R.color.colorAccent));
        description.setTextSize(65);
        description.setText(mContext.getString(R.string.chart_content_description, tvSymbol.getText()));
        chart.setDescription(description);*/
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

        private DecimalFormat dollarFormatWithPlus;

        public StockHawkYAxisValueFormatter() {
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return dollarFormatWithPlus.format(value);
        }
    }


}
