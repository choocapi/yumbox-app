package com.example.yumbox.Utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatString {
    public static String formatAmountFromNumber(int amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount).replace(",", ".") + "đ";
    }

    public static String formatAmountFromString(String amount) {
        int value = Integer.parseInt(amount);
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(value).replace(",", ".") + "đ";
    }
}
