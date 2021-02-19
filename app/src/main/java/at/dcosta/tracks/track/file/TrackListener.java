package at.dcosta.tracks.track.file;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public interface TrackListener {

    void processPoint(Point p);

    void processPoint(Point p, Distance distance, DistanceValidator validator);

}
