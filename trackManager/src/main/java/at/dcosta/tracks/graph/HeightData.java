package at.dcosta.tracks.graph;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.MinMaxValue;
import at.dcosta.tracks.validator.DistanceValidator;

abstract class HeightData extends TrackProfileData {

	public static final int HIGHT_RESOLUTION = -1;

	protected final List<Integer> heights;
	protected final MinMaxValue height;
	private int lastHeight;

	public HeightData() {
		heights = new ArrayList<Integer>();
		height = new MinMaxValue();
	}

	@Override
	public MinMaxValue getYMinMax() {
		return height;
	}

	@Override
	public void processPoint(Point p) {
		int h = p.getHeight();
		int diff = lastHeight - h;
		if (Math.abs(diff) > HIGHT_RESOLUTION) {
			heights.add(h);
			height.update(h);
			lastHeight = h;
			processValidPoint(p);
		} else {
			System.out.println("throw pint away: " + p);
		}
	}

	@Override
	public void processPoint(Point p, Distance distance, DistanceValidator validator) {
		processPoint(p);
	}

	public abstract void processValidPoint(Point p);
}