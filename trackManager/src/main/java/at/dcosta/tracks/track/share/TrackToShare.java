package at.dcosta.tracks.track.share;

import java.io.Serializable;
import java.util.List;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescription;

public class TrackToShare implements Serializable {

	private static final long serialVersionUID = 4666003220123152368L;
	private final TrackDescription description;
	private final List<Point> track;

	public TrackToShare(TrackDescription description, List<Point> track) {
		this.description = description;
		this.track = track;
	}

	public TrackDescription getDescription() {
		return description;
	}

	public List<Point> getTrack() {
		return track;
	}

}
