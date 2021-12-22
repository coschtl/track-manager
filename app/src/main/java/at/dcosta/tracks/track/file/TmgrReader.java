package at.dcosta.tracks.track.file;

import java.util.Iterator;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.TrackIO;
import at.dcosta.tracks.validator.DistanceValidator;

public class TmgrReader extends TrackReader {

    public static final String EXTENSION = "tmgr";
    public static final String SUFFIX = "." + EXTENSION;

    public TmgrReader(Content trackContent, DistanceValidator validator) {
        super(trackContent, validator);
    }

    @Override
    public TmgrReader readTrack() {
        if (listener == null) {
            return this;
        }
        Iterator<Point> it = TrackIO.loadTmgrTrack(trackContent.getInputStream()).iterator();
        while (it.hasNext()) {
            Point point = it.next();
            updateListener(point);
        }
        return this;
    }
}
