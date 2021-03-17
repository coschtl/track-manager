package at.dcosta.tracks.graph.util;

import java.util.Calendar;
import java.util.Date;

import at.dcosta.tracks.util.MinMaxValue;

public class GraphUtil {

    public static MinMaxValue getGraphBoundaries(MinMaxValue timeMinMax, int rasterCount) {
        int minsPerRaster = (int) (timeMinMax.diffAsLong() / 60000L) / rasterCount;
        int shift = 0;
        if (minsPerRaster > 60) {
            minsPerRaster = 120;
        } else if (minsPerRaster > 45) {
            minsPerRaster = 60;
        } else if (minsPerRaster > 30) {
            minsPerRaster = 45;
        } else if (minsPerRaster > 20) {
            minsPerRaster = 30;
        } else if (minsPerRaster > 15) {
            minsPerRaster = 20;
            shift = 5;
        } else if (minsPerRaster > 10) {
            minsPerRaster = 15;
        } else if (minsPerRaster > 5) {
            minsPerRaster = 10;
        } else {
            minsPerRaster = 5;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timeMinMax.minAsLong()));
        int min = cal.get(Calendar.MINUTE);
        if (min > 45 + shift) {
            cal.set(Calendar.MINUTE, 45 + shift);
        } else if (min > 30 + shift) {
            cal.set(Calendar.MINUTE, 30 + shift);
        } else if (min > 15 + shift) {
            cal.set(Calendar.MINUTE, 15 + shift);
        } else {
            cal.set(Calendar.MINUTE, shift);
        }
        long minTime = cal.getTimeInMillis();
        long maxTime = minTime + rasterCount * minsPerRaster * 60000L;
        return new MinMaxValue().update(minTime).update(maxTime);
    }
}
