package at.dcosta.tracks.graph;

import android.content.Context;

import java.text.Format;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.util.MinMaxValue;

public abstract class TrackProfileData implements TrackListener {

    protected long findLengthInterval(long diff) {
        // System.out.println("find length interval for : " + diff);
        diff = diff / 10L;
        if (diff >= 600) {
            return 1000;
        }
        if (diff >= 350) {
            return 500;
        }
        if (diff >= 170) {
            return 200;
        }
        if (diff >= 70) {
            return 100;
        }
        if (diff >= 40) {
            return 50;
        }
        return 10;
    }

    protected long findTimeInterval(long diff) {
        // System.out.println("find time interval for : " + diff);
        if (diff >= DateUtil.HOUR_MILLIS * 150L) {    //6 days
            return DateUtil.DAY_MILLIS;
        }
        if (diff >= DateUtil.HOUR_MILLIS * 60L) {
            return DateUtil.HOUR_MILLIS * 12L;
        }
        if (diff >= DateUtil.HOUR_MILLIS * 20L) {
            return DateUtil.HOUR_MILLIS * 6L;
        }
        if (diff >= DateUtil.HOUR_MILLIS * 10) {
            return DateUtil.HOUR_MILLIS * 2L;
        }
        if (diff >= DateUtil.MINUTE_MILLIS * 300L) {
            return DateUtil.HOUR_MILLIS;
        }
        if (diff >= DateUtil.MINUTE_MILLIS * 150) {
            return DateUtil.MINUTE_MILLIS * 30L;
        }
        if (diff >= DateUtil.MINUTE_MILLIS * 100L) {
            return DateUtil.MINUTE_MILLIS * 15L;
        }
        if (diff >= DateUtil.MINUTE_MILLIS * 50L) {
            return DateUtil.MINUTE_MILLIS * 10L;
        }
        return DateUtil.MINUTE_MILLIS * 5;
    }

    public abstract Format getXFormat();

    public abstract long getXInterval();

    public abstract String getXLabel(Context context);

    public abstract MinMaxValue getXMinMax();

    public abstract com.androidplot.xy.XYSeries getXYSeries();

    public abstract Format getYFormat();

    public abstract long getYInterval();

    public abstract String getYLabel(Context context);

    public abstract MinMaxValue getYMinMax();

}
