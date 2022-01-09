package at.dcosta.tracks.track;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.validator.DistanceValidator;

public class Track implements TrackListener {

    private final List<Point> points;
    private final List<Waypoint> waypoints;

    public Track() {
        points = new ArrayList<Point>();
        waypoints = new ArrayList<Waypoint>();
    }

    public void clear() {
        points.clear();
        waypoints.clear();
    }

    public List<Point> getPoints() {
        return points;
    }

    public int getSize() {
        return points.size();
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public void processPoint(Point p) {
        if (p instanceof Waypoint) {
            waypoints.add((Waypoint) p);
        } else {
            points.add(p);
        }
    }

    @Override
    public void processPoint(Point p, Distance distance, DistanceValidator validator) {
        processPoint(p);
    }
}
