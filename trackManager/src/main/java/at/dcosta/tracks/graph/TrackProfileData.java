package at.dcosta.tracks.graph;

import java.text.Format;

import android.content.Context;
import at.dcosta.android.fw.DateUtil;
import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.util.MinMaxValue;

public abstract class TrackProfileData implements TrackListener {

	protected long findLengthInterval(long diff) {
		// System.out.println("find length interval for : " + diff);
		diff = diff / 10l;
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
		diff = diff / 10l;
		if (diff >= DateUtil.HOUR_MILLIS * 15l) {
			return DateUtil.DAY_MILLIS;
		}
		if (diff >= DateUtil.HOUR_MILLIS * 8l) {
			return DateUtil.HOUR_MILLIS * 12l;
		}
		if (diff >= DateUtil.HOUR_MILLIS * 4l) {
			return DateUtil.HOUR_MILLIS * 6l;
		}
		if (diff >= DateUtil.HOUR_MILLIS * 1.5) {
			return DateUtil.HOUR_MILLIS * 2l;
		}
		if (diff >= DateUtil.MINUTE_MILLIS * 45l) {
			return DateUtil.HOUR_MILLIS;
		}
		if (diff >= DateUtil.MINUTE_MILLIS * 20l) {
			return DateUtil.MINUTE_MILLIS * 30l;
		}
		if (diff >= DateUtil.MINUTE_MILLIS * 10l) {
			return DateUtil.MINUTE_MILLIS * 15l;
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
