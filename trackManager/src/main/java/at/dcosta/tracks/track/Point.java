package at.dcosta.tracks.track;

import java.io.Serializable;
import java.util.Date;

import android.location.Location;

public class Point implements Serializable {

	private static final long serialVersionUID = 5745768757860467417L;

	private double lat, lon;
	private int height;
	private long timestamp;
	private long id, trackId;

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
		double dlat = 111300d * (lat - previousPoint.getLat());
		double dlon = 71500d * (lon - previousPoint.getLon());
		Distance distance = new Distance();
		distance.setHorizontal(Math.sqrt(dlat * dlat + dlon * dlon));
		distance.setVertical(height - previousPoint.getHeight());
		distance.setTime(timestamp - previousPoint.getTimeStampAsLong());
		return distance;
	}

	public int getHeight() {
		return height;
	}

	public long getId() {
		return id;
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

	@Override
	public int hashCode() {
		return Double.valueOf(lat).hashCode() + Double.valueOf(lon).hashCode() + height + Long.valueOf(timestamp).hashCode();
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTimestamp(long timestamp) {
		if (timestamp < 10000000000l) {
			this.timestamp = 1000l * timestamp;
		} else {
			this.timestamp = timestamp;
		}
	}

	public void setTrackId(long trackId) {
		this.trackId = trackId;
	}

	public Point shift(long milliSeconds) {
		timestamp += milliSeconds;
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getTimeStampAsLong()).append(", ").append(getTimeStampAsDate()).append(": lat=").append(lat).append(", lon=")
				.append(lon).append(", h=").append(height).toString();
	}
}
