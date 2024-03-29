package at.dcosta.tracks.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import at.dcosta.tracks.track.Point;

public class PointUtil {

    public static LatLng createGeoPoint(Location location) {
        return new LatLng(getE6(location.getLatitude()), getE6(location.getLongitude()));
    }

    public static LatLng createGeoPoint(Point p) {
        return new LatLng(getE6(p.getLat()), getE6(p.getLon()));
    }

    public static org.osmdroid.util.GeoPoint createOsmGeoPoint(Location location) {
        return new org.osmdroid.util.GeoPoint(getE6(location.getLatitude()), getE6(location.getLongitude()));
    }

    private static int getE6(double d) {
        return (int) (d * 1000000.0d);
    }

}
