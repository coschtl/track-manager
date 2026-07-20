package at.dcosta.tracks.track.file;

import java.io.InputStream;
import java.util.stream.Stream;

import at.dcosta.tracks.combat.Content;

public abstract class FileLocator {

    public enum OverwriteAction {
        FAIL, OVERWRITE, SKIP_SILENT;
    }


    public static class FileMetadata {
        String name;
        String path;

        public FileMetadata(String pathAndName, String[] pathSeparators) {
            int pos = -1;
            int len =0;
            for (String separator : pathSeparators) {
                int sPos = pathAndName.lastIndexOf(separator);
                if (sPos > pos) {
                    pos = sPos;
                    len = separator.length();
                }
            }
            this.name = pathAndName.substring(pos + len);
            this.path = pathAndName.substring(0, pos);
        }
    }

    public static String getFileNameFromPath(String fileName, String[] pathSeparators) {
        return new FileMetadata(fileName, pathSeparators).name;
    }

    public static String getParentFolder(String fileName, String[] pathSeparators) {
        return new FileMetadata(fileName, pathSeparators).path;
    }

    public Stream<Content> list(FolderUri folder, long newerThanEpochMillis) {
        return list(folder, newerThanEpochMillis, false);
    }

    public abstract int getContentCount(FolderUri folder, long newerThanEpochMillis);

    public abstract Stream<Content> list(FolderUri folder, long newerThanEpochMillis, boolean clearCache);

    public abstract Content findPhoto(String fileName);

    public abstract Content findTrack(String trackName);

    public abstract boolean fileExists(FilesystemUri uri);

    public abstract boolean writeToFile (FolderUri parentFolder, String fileName, InputStream content, OverwriteAction overwriteAction);

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
