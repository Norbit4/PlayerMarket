package pl.norbit.playermarket.utils;

import java.text.DecimalFormat;

public class DoubleFormatter {
    private static final DecimalFormat df = new DecimalFormat("0.#");
    public static String format(double d){
        return df.format(d);
    }
}
