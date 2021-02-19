package at.dcosta.tracks.track;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;

public class Point implements Serializable {

	private static final long serialVersionUID = 5745768757860467417L;

	private final double lat;
	private final double lon;
	private int height;
	private long timestamp;
	private long id, trackId;
	private int pulse;

	public Point(double lat, double lon, int height, long timestamp) {
		this.lat = lat;
		this.lon = lon;
		this.height = height;
		setTimestamp(timestamp);
	}

	public Point(Location location) {
		this(location.getLatitude(), location.getLongitude(), (int) location.getAltitude(), location.getTime());
	}

	public Point(Point point) {
		this(point.getLat(), point.getLon(), point.getHeight(), point.getTimeStampAsLong());
	}

	public Point(String latLonHeightTimestamp) {
		try {
			// (47.22325897216797,11.527511596679688,2039.0,1612203827,152)
			String[] s = latLonHeightTimestamp.split(",");

			lat = Double.parseDouble(s[0]);
			lon = Double.parseDouble(s[1]);
			int pos = s[2].indexOf('.');
			if (pos >= 0) {
				height = Integer.parseInt(s[2].substring(0, pos));
			} else {
				height = Integer.parseInt(s[2]);
			}
			if (s.length > 3) {
				setTimestamp(Long.parseLong(s[3]));
			} else {
				setTimestamp(0);
			}
			if (s.length > 4) {
				pulse = Integer.parseInt(s[4]);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Can not parse the given String: " + e.toString() + "\nPlease use the Format 'lat,lon,height,timestamp)");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point p = (Point) o;
			return lat == p.lat && lon == p.lon && height == p.height && timestamp == p.timestamp;
		}
		return false;
	}

	public Distance getDistance(Point previousPoint) {
		double latMeters = 111300L * (lat - previousPoint.getLat());
		double lonMeters = 71500L * (lon - previousPoint.getLon());
		Distance distance = new Distance();
		distance.setHorizontal((long) Math.sqrt(latMeters * latMeters + lonMeters * lonMeters));
		distance.setVertical(height - previousPoint.getHeight());
		distance.setTime(timestamp - previousPoint.getTimeStampAsLong());
		return distance;
	}

	public int getHeight() {
		return height;
	}

	public Point setHeight(int height) {
		this.height = height;
		return this;
	}

	public long getId() {
		return id;
	}

	public Point setId(long id) {
		this.id = id;
		return this;
	}

	public int getPulse() {
		return pulse;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public Date getTimeStampAsDate() {
		return new Date(timestamp);
	}

	public long getTimeStampAsLong() {
		return timestamp;
	}

	public long getTrackId() {
		return trackId;
	}

	public Point setTrackId(long trackId) {
		this.trackId = trackId;
		return this;
	}

	@Override
	public int hashCode() {
		return Double.valueOf(lat).hashCode() + Double.valueOf(lon).hashCode() + height + Long.valueOf(timestamp).hashCode();
	}

	public Point setTimestamp(long timestamp) {
		if (timestamp < 10000000000l) {
			this.timestamp = 1000l * timestamp;
		} else {
			this.timestamp = timestamp;
		}
		return this;
	}

	public Point shift(long milliSeconds) {
		timestamp += milliSeconds;
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getTimeStampAsLong()).append(", ").append(getTimeStampAsDate()).append(": lat=").append(lat).append(", lon=")
				.append(lon).append(", h=").append(height).append(", pulse=").append(pulse).append(", ts=").append(timestamp).toString();
	}
}
