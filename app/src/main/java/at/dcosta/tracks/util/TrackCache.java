package at.dcosta.tracks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.file.TmgrReader;

public class TrackCache {

    // public static final String COPY = "copy_";
    // private static final String COPY_PREFIX = "_" + COPY;

    private final File trackDir;

    public TrackCache() {
        Configuration config = Configuration.getInstance();
        trackDir = new File(config.getWorkingDir() + "/tracks");
        if (!trackDir.isDirectory()) {
            trackDir.mkdir();
        }
    }

    public void clear() {
        String[] tracks = trackDir.list();
        for (String track : tracks) {
            // if (!track.startsWith(COPY_PREFIX)) {
            new File(trackDir, track).delete();
            // }
        }
    }

    public void copy(long originalRowId, long newRowId, long timeDiffMillis) {
        List<Point> track = load(originalRowId);
        for (Point point : track) {
            point.shift(timeDiffMillis);
        }
        save(newRowId, track);
    }

    public void delete(long rowId) {
        File file = getFile(rowId);
        if (file != null) {
            file.delete();
        }
    }

    private File getFile(long rowId) {
        if (rowId < 0) {
            return null;
        }
        return new File(trackDir, rowId + TmgrReader.SUFFIX);
    }

    public List<Point> load(long rowId) {
        if (rowId < 0) {
            return Collections.emptyList();
        }
        File file = getFile(rowId);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (FileInputStream in = new FileInputStream(file)) {
            return TrackIO.loadTmgrTrack(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public File save(long rowId, List<Point> points) {
        if (rowId < 0 || points == null) {
            return null;
        }
        File file = getFile(rowId);
        if (file.exists()) {
            file.delete();
        }
        if (TrackIO.writeTmgrTrack(file, points)) {
            return file;
        }
        return null;
    }
}
