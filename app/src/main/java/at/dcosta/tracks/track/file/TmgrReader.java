package at.dcosta.tracks.track.file;

import java.io.File;
import java.util.Iterator;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.TrackIO;
import at.dcosta.tracks.validator.DistanceValidator;

public class TmgrReader extends TrackReader {

    public static final String EXTENSION = "tmgr";
    public static final String SUFFIX = "." + EXTENSION;

    public TmgrReader(File trackfile, DistanceValidator validator) {
        super(trackfile, validator);
    }

    @Override
    public TmgrReader readTrack() {
        if (listener == null) {
            return this;
        }
        Iterator<Point> it = TrackIO.loadTmgrTrack(trackfile).iterator();
        while (it.hasNext()) {
            Point point = it.next();
            updateListener(point);
        }
        return this;
    }
}
