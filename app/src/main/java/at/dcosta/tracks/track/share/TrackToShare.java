package at.dcosta.tracks.track.share;

import java.io.Serializable;
import java.util.List;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescriptionNG;

public class TrackToShare implements Serializable {

    private static final long serialVersionUID = 4666003220123152368L;
    private final TrackDescriptionNG description;
    private final List<Point> track;

    public TrackToShare(TrackDescriptionNG description, List<Point> track) {
        this.description = description;
        this.track = track;
    }

    public TrackDescriptionNG getDescription() {
        return description;
    }

    public List<Point> getTrack() {
        return track;
    }

}
