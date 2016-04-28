package at.dcosta.tracks.util;

import android.location.Location;
import at.dcosta.tracks.track.Point;

import com.google.android.maps.GeoPoint;

public class PointUtil {

	public static GeoPoint createGeoPoint(Location location) {
		return new GeoPoint(getE6(location.getLatitude()), getE6(location.getLongitude()));
	}

	public static GeoPoint createGeoPoint(Point p) {
		return new GeoPoint(getE6(p.getLat()), getE6(p.getLon()));
	}

	public static org.osmdroid.util.GeoPoint createOsmGeoPoint(Location location) {
		return new org.osmdroid.util.GeoPoint(getE6(location.getLatitude()), getE6(location.getLongitude()));
	}

	private static int getE6(double d) {
		return (int) (d * 1000000.0d);
	}

}
