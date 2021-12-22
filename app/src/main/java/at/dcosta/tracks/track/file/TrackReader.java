package at.dcosta.tracks.track.file;

import java.util.logging.Level;
import java.util.logging.Logger;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.HeightFlattener;

public abstract class TrackReader {

    private static final Logger LOGGER = Logger.getLogger(TrackReader.class.getName());

    final Content trackContent;
    private final DistanceValidator distanceValidator;
    private final HeightFlattener flattener;
    TrackListener[] listener;
    String trackName;
    int pointsRead;
    int readLimit = Integer.MAX_VALUE;
    private Point lastPoint;

    public TrackReader(Content trackContent, DistanceValidator distanceValidator) {
        this.trackContent = trackContent;
        this.distanceValidator = distanceValidator;
        flattener = new HeightFlattener(Configuration.getInstance());
        if (trackContent != null) {
            trackName = trackContent.getName();
            int pos = trackName.lastIndexOf('.');
            trackName = trackName.substring(0, pos);
        }
    }

    public String getTrackName() {
        return trackName;
    }

    private void notifyTrackListeners(Point point, Distance distance) {
        for (TrackListener l : listener) {
            l.processPoint(point, distance, distanceValidator);
        }
    }

    public abstract TrackReader readTrack() throws ParsingException;

    public TrackReader setListener(TrackListener... listener) {
        this.listener = listener;
        return this;
    }

    public void setReadLimit(int readLimit) {
        this.readLimit = readLimit;
    }

    void updateListener(Point point) {
        //System.out.print("Got raw point: " + point);
        if (lastPoint == null) {
            point.setHeight(flattener.getFattenedHeight(point));
            for (TrackListener l : listener) {
                l.processPoint(point);
            }
            lastPoint = point;
        } else {
            Distance distance = point.getDistance(lastPoint);
            distanceValidator.setDistance(distance);
            if (true || distanceValidator.isValid()) {
                int flattenedHeight = flattener.getFattenedHeight(point);
                point.setHeight(flattenedHeight);
                distance = point.getDistance(lastPoint);
                lastPoint = point;
            } else {
                System.out.println("Point is not valid: " + distanceValidator.getInvalidReason() + " : " + point);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Point is not valid: " + distanceValidator.getInvalidReason() + " : " + point);
                }
                long timeStamp = point.getTimeStampAsLong();
                point = new Point(lastPoint);
                point.setTimestamp(timeStamp);
            }
            notifyTrackListeners(point, distance);
        }
    }
}
