package at.dcosta.tracks.graph;

import android.content.Context;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracks.R;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.MinMaxValue;

final class HeightTimeData extends HeightData {

    private final List<Long> timestamps;
    private final MinMaxValue time;

    public HeightTimeData() {
        timestamps = new ArrayList<Long>();
        time = new MinMaxValue();
    }

    @Override
    public Format getXFormat() {
        return new SimpleDateFormat("HH:mm");
    }

    @Override
    public long getXInterval() {
        return findTimeInterval(getXMinMax().diffAsLong());
    }

    @Override
    public String getXLabel(Context context) {
        return context.getString(R.string.time);
    }

    @Override
    public MinMaxValue getXMinMax() {
        return time;
    }

    @Override
    public XYSeries getXYSeries() {
        return new SimpleXYSeries(timestamps, heights, "");
    }

    @Override
    public Format getYFormat() {
        return new DecimalFormat("0");
    }

    @Override
    public long getYInterval() {
        return findLengthInterval(getYMinMax().diffAsLong());
    }

    @Override
    public String getYLabel(Context context) {
        return context.getString(R.string.height);
    }

    @Override
    public void processValidPoint(Point p) {
        long ts = p.getTimeStampAsLong();
        timestamps.add(ts);
        time.update(ts);
    }

}