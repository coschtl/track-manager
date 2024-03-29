package at.dcosta.tracks.track.file;

import android.net.Uri;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.util.Configuration;

public abstract class FileLocator {

    public static String getFileNameFromPath(String fileName, String[] pathSeparators) {
        int i = 0;
        int pos = -1;
        String pathSeparator;
        do {
            pathSeparator = pathSeparators[i];
            pos = fileName.lastIndexOf(pathSeparator);
            i++;
        } while (pos < 0 && i < pathSeparators.length);
        if (pos >= 0) {
            return fileName.substring(pos + pathSeparator.length());
        }
        return fileName;
    }

    public Stream<Content> list(Uri path, long newerThanEpochMillis) {
        return list(path, newerThanEpochMillis, false);
    }

    public abstract int getContentCount(Uri path, long newerThanEpochMillis);

    public abstract Stream<Content> list(Uri path, long newerThanEpochMillis, boolean clearCache);

    public abstract Content findPhoto(String fileName);

    public abstract Content findTrack(String trackName);

    public abstract boolean fileExists(String fullFilePath, boolean isTreeUri);


    public boolean fileExists(String fullFilePath) {
        return fileExists(fullFilePath, false);
    }

    public abstract String[] getPathSeparators();

    protected String getPlainTrackNameNoSuffix(String trackName) {
        // remove suffix
        String plainTrackName = getFileNameFromPath(trackName, getPathSeparators());
        int pos = plainTrackName.lastIndexOf('.');
        if (pos >= 0) {
            return plainTrackName.substring(0, pos);
        }
        return plainTrackName;
    }
}
