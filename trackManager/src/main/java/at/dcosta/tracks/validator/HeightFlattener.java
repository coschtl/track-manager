package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.Configuration;

public class HeightFlattener {

	private final FixedLengthLiFoBuffer<Double> buffer;
	private final boolean disabled;
	private Point lastPoint;

	private static final double[] WEIGHTS;
	static {
		// WEIGHTS = new double[] { 12, 8, 6, 4, 2 };
		WEIGHTS = new double[] { 1, 0, 1, 1, 1 };
	}

	public HeightFlattener(Configuration config) {
		disabled = config.getSingleValueDbProperty("heightFlattenerDisabled").getBooleanValue(false);
		buffer = new FixedLengthLiFoBuffer<Double>(5);
	}

	public int getFattenedHeight(Point p) {
		if (disabled) {
			return p.getHeight();
		}
		if (lastPoint == null) {
			lastPoint = p;
			return p.getHeight();
		}
		Distance d = p.getDistance(lastPoint);
		double k = d.getVertical() / d.getHorizontal();

		buffer.add(k);
		k = 0;
		int pos = 0;
		double weight = 0;
		while (buffer.hasMoreElements()) {
			weight += WEIGHTS[pos];
			k = k + buffer.nextElement() * WEIGHTS[pos];
			pos++;
		}
		k = k / weight;
		return ((int) (lastPoint.getHeight() + k * d.getHorizontal()) + p.getHeight()) / 2;

	}

	public void reset(Point p) {
		lastPoint = null;
		buffer.clear();
	}
}
