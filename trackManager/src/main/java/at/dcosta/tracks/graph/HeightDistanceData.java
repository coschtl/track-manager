package at.dcosta.tracks.graph;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import at.dcosta.tracks.R;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.MinMaxValue;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

final class HeightDistanceData extends HeightData {

	private final List<Double> distances;
	private final MinMaxValue distance;
	private Point previousPoint;
	private double totalDistance;

	public HeightDistanceData() {
		distances = new ArrayList<Double>();
		distance = new MinMaxValue();
	}

	@Override
	public Format getXFormat() {
		return new DecimalFormat("0");
	}

	@Override
	public long getXInterval() {
		return findLengthInterval(getXMinMax().diffAsLong());
	}

	@Override
	public String getXLabel(Context context) {
		return context.getString(R.string.distance);
	}

	@Override
	public MinMaxValue getXMinMax() {
		return distance;
	}

	@Override
	public XYSeries getXYSeries() {
		return new SimpleXYSeries(distances, heights, "");
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
		double dist;
		if (previousPoint == null) {
			dist = 0.0;
		} else {
			dist = p.getDistance(previousPoint).getHorizontal();
		}
		totalDistance += dist;
		distances.add(totalDistance);
		distance.update((long) totalDistance);
		previousPoint = p;
	}

}