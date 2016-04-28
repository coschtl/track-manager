package at.dcosta.tracks.track.file;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public interface TrackListener {

	public void processPoint(Point p);

	public void processPoint(Point p, Distance distance, DistanceValidator validator);

}
