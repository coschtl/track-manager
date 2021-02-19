package at.dcosta.tracks.graph.util;

import com.androidplot.xy.OrderedXYSeries;
import com.androidplot.xy.SimpleXYSeries;

import java.util.List;

public class TrackDataXySeries extends SimpleXYSeries implements OrderedXYSeries {
    public TrackDataXySeries(String title) {
        super(title);
    }

    public TrackDataXySeries(ArrayFormat format, String title, Number... model) {
        super(format, title, model);
    }

    public TrackDataXySeries(List<? extends Number> model, ArrayFormat format, String title) {
        super(model, format, title);
    }

    public TrackDataXySeries(List<? extends Number> xVals, List<? extends Number> yVals, String title) {
        super(xVals, yVals, title);
    }
}
