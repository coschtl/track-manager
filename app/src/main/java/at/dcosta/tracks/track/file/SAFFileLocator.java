package at.dcosta.tracks.track.file;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.SAFContent;

public class SAFFileLocator extends FileLocator {

    public static final String[] PATH_SEPARATORS = new String[]{"%2F", "/"};

    private static final Logger LOGGER = Logger.getLogger(SAFFileLocator.class.getName());
    private static final Map<Uri, List<Content>> CACHE = new HashMap<>();

    private final Context context;

    public SAFFileLocator(Context context) {
        this.context = context;
    }

    @Override
    public int getContentCount(Uri path) {
        if (path == null) {
            return 0;
        }
        return getDirContent(path, false).size();
    }

    @Override
    public Stream<Content> list(Uri path, boolean clearCache) {
        if (path == null) {
            return Stream.empty();
        }
        return getDirContent(path, clearCache).stream();
    }

    @Override
    Content findFile(String fileName, List<Uri> possibleFolders, Function<String, String> plainNameEvaluation) {
        if (fileExists(fileName)) {
            return new SAFContent(context, Uri.parse(fileName));
        }
        String plainFileName = null;
        plainFileName = Uri.encode(plainNameEvaluation.apply(fileName), "utf-8");
        for (Uri trackFolder : possibleFolders) {
            for (Content content : getDirContent(trackFolder, false)) {
                if (content.getFullPath().indexOf(plainFileName) != -1) {
                    return content;
                }
            }
        }
        return null;
    }

    private List<Content> getDirContent(Uri path, boolean clearCache) {
        synchronized (SAFFileLocator.class) {
            if (!CACHE.containsKey(path) || clearCache) {
                LOGGER.info("Reading content of " + path);
                DocumentFile dir = DocumentFile.fromTreeUri(context, path);
                List<Content> files = Arrays.stream(dir.listFiles()).map(file -> new SAFContent(context, file.getUri())).collect(Collectors.toList());
                LOGGER.info(path + " contains " + files.size());
                System.out.println("--------------------------");
                for (Content c : files) {
                    System.out.println(c.getFullPath());
                }
                System.out.println("--------------------------");
                CACHE.put(path, files);
            }
            return CACHE.get(path);
        }
    }

    @Override
    public boolean fileExists(String fullFilePath, boolean isTreeUri) {
        Uri uri = Uri.parse(fullFilePath);
        try {
            DocumentFile file = isTreeUri ? DocumentFile.fromTreeUri(context, uri) : DocumentFile.fromSingleUri(context, uri);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    @Override
    public String[] getPathSeparators() {
        return PATH_SEPARATORS;
    }
}
