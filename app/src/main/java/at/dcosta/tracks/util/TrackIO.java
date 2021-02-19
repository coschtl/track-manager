package at.dcosta.tracks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.Track;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.validator.Validators;

public class TrackIO {

    public static File getRecordedTrackFile(String filename) {
        return new File(Configuration.getInstance().getRecordedTracksDir(), filename);
    }

    public static List<Point> loadTmgrTrack(File file) {
        if (file == null) {
            return Collections.emptyList();
        }
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            @SuppressWarnings("unchecked")
            List<Point> points = (List<Point>) in.readObject();
            in.close();
            return points;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<Point> loadTrack(File file) throws ParsingException {
        Track track = new Track();
        TrackReader reader = TrackReaderFactory.getTrackReader(file, Validators.DEFAULT);
        reader.setListener(track);
        reader.readTrack();
        return track.getPoints();
    }

    public static boolean writeTmgrTrack(File file, List<Point> points) {
        if (file == null || points == null) {
            return false;
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(points);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
