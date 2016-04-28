package at.dcosta.tracks.track.file;

import java.io.File;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.HeightFlattener;

public abstract class TrackReader {

	final File trackfile;
	TrackListener[] listener;
	String trackName;
	int pointsRead;
	private Point lastPoint;
	private final DistanceValidator distanceValidator;
	private final HeightFlattener flattener;

	int readLimit = Integer.MAX_VALUE;

	public TrackReader(File trackfile, DistanceValidator distanceValidator) {
		this.trackfile = trackfile;
		this.distanceValidator = distanceValidator;
		flattener = new HeightFlattener(Configuration.getInstance());
		if (trackfile != null) {
			trackName = trackfile.getName();
			int pos = trackName.lastIndexOf('.');
			trackName = trackName.substring(0, pos);
		}
	}

	public String getTrackName() {
		return trackName;
	}

	private void notifyTrackListeners(Point point, Distance distance) {
		for (TrackListener l : listener) {
			l.processPoint(point, distance, distanceValidator);
		}
	}

	public abstract TrackReader readTrack() throws ParsingException;

	public TrackReader setListener(TrackListener... listener) {
		this.listener = listener;
		return this;
	}

	public void setReadLimit(int readLimit) {
		this.readLimit = readLimit;
	}

	void updateListener(Point point) {
		if (lastPoint == null) {
			point.setHeight(flattener.getFattenedHeight(point));
			for (TrackListener l : listener) {
				l.processPoint(point);
			}
			lastPoint = point;
		} else {
			Distance distance = point.getDistance(lastPoint);
			distanceValidator.setDistance(distance);
			if (distanceValidator.isValid()) {
				point.setHeight(flattener.getFattenedHeight(point));
				distance = point.getDistance(lastPoint);
				lastPoint = point;
			} else {
				long timeStamp = point.getTimeStampAsLong();
				point = new Point(lastPoint);
				point.setTimestamp(timeStamp);
				if (!distanceValidator.isMoving()) {
					flattener.reset(point);
				}
				distance = point.getDistance(lastPoint);
				distanceValidator.setDistance(distance);
			}
			notifyTrackListeners(point, distance);
		}
	}
}
