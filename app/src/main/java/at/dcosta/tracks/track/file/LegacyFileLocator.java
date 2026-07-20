package at.dcosta.tracks.track.file;

import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.FileContent;
import at.dcosta.tracks.util.Configuration;

public class LegacyFileLocator extends FileLocator {

    public static final String[] PATH_SEPARATORS = new String[]{"/"};
    private List<File> allTrackFiles;
    private List<File> allPhotoFiles;

    @Override
    public int getContentCount(FolderUri path, long newerThanEpochMillis) {
        if (path == null) {
            return 0;
        }
        if (newerThanEpochMillis == -1) {
            return new File(path.toString()).listFiles().length;
        }
        return new File(path.toString()).listFiles(file -> file.lastModified() > newerThanEpochMillis).length;
    }

    @Override
    public Stream<Content> list(FolderUri path, long newerThanEpochMillis, boolean clearCache) {
        if (path == null) {
            return Stream.empty();
        }
        if (newerThanEpochMillis >= 0) {
            return Arrays.stream(new File(path.toString()).listFiles()).map(file -> new FileContent(file));
        }
        return Arrays.stream(new File(path.toString()).listFiles(file -> file.lastModified() > newerThanEpochMillis)).map(file -> new FileContent(file));
    }

    @Override
    public Content findPhoto(String fileName) {
        if (allPhotoFiles == null) {
            allPhotoFiles = readAllFilesFromFolders(Configuration.getInstance().getPhotoFolders());
        }
        return findFileContent(fileName, allPhotoFiles, name -> getFileNameFromPath(name, getPathSeparators()));
    }

    @Override
    public Content findTrack(String trackName) {
        if (allTrackFiles == null) {
            allTrackFiles = readAllFilesFromFolders(Configuration.getInstance().getTrackFolders());
        }
        return findFileContent(trackName,allTrackFiles, this::getPlainTrackNameNoSuffix);
    }

    private Content findFileContent(String fileName, List<File> allFiles, Function<String, String> plainNameEvaluation) {
        if (fileExists(new FileUri(fileName))) {
            return new FileContent(new File(fileName));
        }
        try {
            String plainFileName = plainNameEvaluation.apply(fileName);
            String decodedFileName = URLDecoder.decode(plainFileName, StandardCharsets.UTF_8.name());
            decodedFileName = decodedFileName.substring(decodedFileName.lastIndexOf("/") + 1);
            for (File file : allFiles) {
                if (file.getName().indexOf(plainFileName) != -1 || file.getName().indexOf(decodedFileName) != -1) {
                    return new FileContent(file);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<File> readAllFilesFromFolders(List<FolderUri> folders) {
        List<File> l = new ArrayList<>();
        if (folders != null) {
            for (FolderUri trackFolder : folders) {
                File dir = new File(trackFolder.toString());
                File[] files = dir.listFiles();
                if (files != null)
                    l.addAll(Arrays.asList(files));
            }
        }
        return l;
    }

    @Override
    public boolean fileExists(FilesystemUri uri) {
        File file = new File(uri.getUri().toString());
        return file.exists() && file.canRead();
    }

    @Override
    public boolean writeToFile (FolderUri parentFolder, String fileName, InputStream content, OverwriteAction overwriteAction) {
        File file = new File(parentFolder.getUri().toString(), fileName);
        if (file.exists()) {
            if (overwriteAction == OverwriteAction.FAIL) {
                throw new IllegalArgumentException("File already exists: " + file.getAbsolutePath());
            }
            if (overwriteAction == OverwriteAction.SKIP_SILENT) {
                return false;
            }
        }
        if (!file.canWrite()) {
            throw new IllegalArgumentException("File not writable: " + file.getAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtil.copy(content, out);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getPathSeparators() {
        return PATH_SEPARATORS;
    }
}
