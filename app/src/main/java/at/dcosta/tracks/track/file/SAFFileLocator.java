package at.dcosta.tracks.track.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.SAFContent;

public class SAFFileLocator extends FileLocator {

    public static final String[] PATH_SEPARATORS = new String[]{"%2F", "/"};
    private static final Logger LOGGER = Logger.getLogger(SAFFileLocator.class.getName());
    private static final Map<CacheKey, List<Content>> CACHE = new HashMap<>();
    private final Context context;

    public SAFFileLocator(Context context) {
        this.context = context;
    }

    @Override
    public int getContentCount(Uri path, long newerThanEpochMillis) {
        if (path == null) {
            return 0;
        }
        return getDirContent(path, newerThanEpochMillis, false).size();
    }

    @Override
    public Stream<Content> list(Uri path, long newerThanEpochMillis, boolean clearCache) {
        if (path == null) {
            return Stream.empty();
        }
        return getDirContent(path, newerThanEpochMillis, clearCache).stream();
    }

    @Override
    Content findFile(String fileName, List<Uri> possibleFolders, Function<String, String> plainNameEvaluation) {
        if (fileExists(fileName)) {
            return new SAFContent(context, Uri.parse(fileName), getModificationDate(fileName));
        }
        String plainFileName = null;
        plainFileName = Uri.encode(plainNameEvaluation.apply(fileName), "utf-8");
        for (Uri folder : possibleFolders) {
            // FIXME: andere methode verwenden, ev. mit angepasster suche
            for (Content content : getDirContent(folder, -1, false)) {
                if (content.getFullPath().indexOf(plainFileName) != -1) {
                    return content;
                }
            }
        }
        return null;
    }

    private List<Content> getDirContent(Uri path, long newerThanEpochMillis, boolean clearCache) {
        synchronized (SAFFileLocator.class) {
            CacheKey key = new CacheKey(path, newerThanEpochMillis);
            if (!CACHE.containsKey(key) || clearCache) {
                LOGGER.info("Reading content of " + path);
                // FIXME: angepasste suche mit newerThan berücksichtigung
                long start = System.currentTimeMillis();
                //DocumentFile dir = DocumentFile.fromTreeUri(context, path);
//                List<Content> files = Arrays.stream(dir.listFiles()).map(file -> new SAFContent(context, file.getUri(), new Date(file.lastModified()))).collect(Collectors.toList());
                List<Content> files = listFiles(path, newerThanEpochMillis);
                LOGGER.info(path + " contains " + files.size() + " files (list took " + (System.currentTimeMillis() - start) + "ms).");
                CACHE.put(key, files);
            }
            return CACHE.get(key);
        }
    }

    public List<Content> listFiles(Uri dir, long newerThanEpochMillis) {
        Uri uri = DocumentsContract.buildDocumentUriUsingTree(dir, DocumentsContract.getTreeDocumentId(dir));
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        final List<Content> results = new ArrayList<>();
        Cursor c = null;
        try {
            String[] cols = new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED};
            c = context.getContentResolver().query(childrenUri, cols, null, null, null);
            while (c.moveToNext()) {
               // System.out.println(c.getString(0) + " - " + c.getString(1) + " ts: " + c.getLong(2) +" date: " + new Date(c.getLong(2)));
                // android seems to ignore a selection when executing the query inside a special protected system folder
                long lastModified = c.getLong(2);
                if (lastModified > newerThanEpochMillis) {
                    final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, c.getString(0));
                    results.add(new SAFContent(context, documentUri, new Date(c.getLong(2))));
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed query: " + e);
        } finally {
            IOUtil.close(c, true);
        }
        return results;
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

    public Date getModificationDate(String fullFilePath) {
        Uri uri = Uri.parse(fullFilePath);
        try {
            DocumentFile file = DocumentFile.fromSingleUri(context, uri);
            return new Date(file.lastModified());
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            return new Date(0);
        }
    }

    @Override
    public String[] getPathSeparators() {
        return PATH_SEPARATORS;
    }

    private static class CacheKey {
        private final Uri uri;
        private final long newerThan;

        public CacheKey(Uri uri, long newerThan) {
            this.uri = uri;
            this.newerThan = newerThan;
        }

        public long getNewerThan() {
            return newerThan;
        }

        public Uri getUri() {
            return uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return uri.equals(cacheKey.uri) && Objects.equals(newerThan, cacheKey.newerThan);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, newerThan);
        }
    }
}
