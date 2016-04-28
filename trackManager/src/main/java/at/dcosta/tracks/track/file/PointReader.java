package at.dcosta.tracks.track.file;

import java.util.List;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public class PointReader extends TrackReader {

	private final List<Point> points;

	public PointReader(List<Point> points, DistanceValidator distanceValidator) {
		super(null, distanceValidator);
		this.points = points;
	}

	@Override
	public TrackReader readTrack() throws ParsingException {
		for (Point point : points) {
			updateListener(point);
		}
		return this;
	}

}
