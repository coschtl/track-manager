package at.dcosta.tracks.track.file;

import android.net.Uri;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.FileContent;

public class LegacyFileLocator extends FileLocator {

    public static final String[] PATH_SEPARATORS = new String[]{"/"};

    @Override
    public int getContentCount(Uri path, long newerThanEpochMillis) {
        if (path == null) {
            return 0;
        }
        if (newerThanEpochMillis == -1) {
            return new File(path.toString()).listFiles().length;
        }
        return new File(path.toString()).listFiles(file -> file.lastModified() > newerThanEpochMillis).length;
    }

    @Override
    public Stream<Content> list(Uri path, long newerThanEpochMillis, boolean clearCache) {
        if (path == null) {
            return Stream.empty();
        }
        if (newerThanEpochMillis >= 0) {
            return Arrays.stream(new File(path.toString()).listFiles()).map(file -> new FileContent(file));
        }
        return Arrays.stream(new File(path.toString()).listFiles(file -> file.lastModified() > newerThanEpochMillis)).map(file -> new FileContent(file));
    }

    @Override
    Content findFile(String fileName, List<Uri> possibleFolders, Function<String, String> plainNameEvaluation) {
        if (fileExists(fileName)) {
            return new FileContent(new File(fileName));
        }
        String plainFileName = plainNameEvaluation.apply(fileName);
        for (Uri trackFolder : possibleFolders) {
            File dir = new File(trackFolder.toString());
            for (File file : dir.listFiles()) {
                if (file.getName().indexOf(plainFileName) != -1) {
                    return new FileContent(file);
                }
            }
        }
        return null;
    }

    @Override
    public boolean fileExists(String fullTrackPath, boolean isTreeUri) {
        File file = new File(fullTrackPath);
        return file.exists() && file.canRead();
    }

    @Override
    public String[] getPathSeparators() {
        return PATH_SEPARATORS;
    }
}
