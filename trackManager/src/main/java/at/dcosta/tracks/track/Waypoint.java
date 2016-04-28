package at.dcosta.tracks.track;

public class Waypoint extends Point {

	private static final long serialVersionUID = 1L;
	private final String name;

	public Waypoint(double lat, double lon, int height, long timestamp, String name) {
		super(lat, lon, height, timestamp);
		this.name = name;
	}

	public Waypoint(Point p, String name) {
		super(p.getLat(), p.getLon(), p.getHeight(), p.getTimeStampAsLong());
		this.name = name;

	}

	public Waypoint(String latLonHeightTimestamp, String name) {
		super(latLonHeightTimestamp);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return new StringBuilder(super.toString()).append(", ").append(name).toString();
	}
}
