package com.udacity.stockhawk.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by goyo on 15/5/17.
 */

public class MoneyFormatUtils {

    static public DecimalFormat dollarFormat;
    static public DecimalFormat percentageFormat;

    /**
     * Format a float value as a String with a sign prefix and the dollar symbol.
     * @param value The value that we want to format.
     * @return The formated String.
     */
    public static String formatDollarWithSign(float value) {
        DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        return dollarFormatWithPlus.format(value);
    }

    /**
     * Format a float value as a String that will only have sign prefix for negative values.
     * All values will be preceded by a dollar sign.
     * @param value The value that we want to format.
     * @return The formated String.
     */
    public static String formatDollar(float value) {
        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        return dollarFormat.format(value);
    }

    /**
     * Format a float value as a String representation of a percentage
     * @param value The value that we want to format.
     * @return The formated String.
     */
    public static String formatPercentage(float value) {
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        return percentageFormat.format(value);
    }

}
