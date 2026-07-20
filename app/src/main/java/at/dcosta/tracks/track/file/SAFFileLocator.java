package at.dcosta.tracks.track.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import at.dcosta.tracks.util.Configuration;

public class SAFFileLocator extends FileLocator {

    public static final String[] PATH_SEPARATORS = new String[]{"%2F", "/"};
    private static final Logger LOGGER = Logger.getLogger(SAFFileLocator.class.getName());
    private static final Map<CacheKey, List<Content>> CACHE = new HashMap<>();
    private final Context context;

    public SAFFileLocator(Context context) {
        this.context = context;
    }

    @Override
    public int getContentCount(FolderUri uri, long newerThanEpochMillis) {
        if (uri == null) {
            return 0;
        }
        return getDirContent(uri, newerThanEpochMillis, false).size();
    }

    @Override
    public Stream<Content> list(FolderUri uri, long newerThanEpochMillis, boolean clearCache) {
        if (uri == null) {
            return Stream.empty();
        }
        return getDirContent(uri, newerThanEpochMillis, clearCache).stream();
    }

    @Override
    public Content findPhoto(String fileName) {
        return findFile(fileName, Configuration.getInstance().getPhotoFolders(), name -> getFileNameFromPath(name, getPathSeparators()));
    }

    @Override
    public Content findTrack(String trackName) {
        return findFile(trackName, Configuration.getInstance().getTrackFolders(), this::getPlainTrackNameNoSuffix);
    }

    private Content findFile(String fileName, List<FolderUri> possibleFolders, Function<String, String> plainNameEvaluation) {
        if (fileExists(new FileUri(fileName))) {
            return new SAFContent(context, Uri.parse(fileName), getModificationDate(new FileUri(fileName)));
        }
        String plainFileName;
        plainFileName = Uri.encode(plainNameEvaluation.apply(fileName), "utf-8");
        for (FolderUri folder : possibleFolders) {
            // FIXME: andere methode verwenden, ev. mit angepasster suche
            for (Content content : getDirContent(folder, -1, false)) {
                if (content.getFullPath().contains(plainFileName)) {
                    return content;
                }
            }
        }
        return null;
    }

    private List<Content> getDirContent(FolderUri uri, long newerThanEpochMillis, boolean clearCache) {
        synchronized (SAFFileLocator.class) {
            CacheKey key = new CacheKey(uri.getUri(), newerThanEpochMillis);
            if (!CACHE.containsKey(key) || clearCache) {
                LOGGER.info("Reading content of " + uri.getUri());
                // FIXME: angepasste suche mit newerThan berücksichtigung
                long start = System.currentTimeMillis();
                //DocumentFile dir = DocumentFile.fromTreeUri(context, path);
//                List<Content> files = Arrays.stream(dir.listFiles()).map(file -> new SAFContent(context, file.getUri(), new Date(file.lastModified()))).collect(Collectors.toList());
                List<Content> files = listFiles(uri, newerThanEpochMillis);
                LOGGER.info(uri.getUri() + " contains " + files.size() + " files (list took " + (System.currentTimeMillis() - start) + "ms).");
                CACHE.put(key, files);
            }
            return CACHE.get(key);
        }
    }

    public List<Content> listFiles(FolderUri dir, long newerThanEpochMillis) {
        Uri uri = DocumentsContract.buildDocumentUriUsingTree(dir.getUri(), DocumentsContract.getTreeDocumentId(dir.getUri()));
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        final List<Content> results = new ArrayList<>();
        Cursor c = null;
        try {
            String[] cols = new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED};
            c = context.getContentResolver().query(childrenUri, cols, null, null, null);
            while (c!= null && c.moveToNext()) {
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
    public boolean fileExists(FilesystemUri uri) {
        try {
            DocumentFile file = getFile(uri);
            return file != null && file.exists() && file.canRead();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean writeToFile(FolderUri parentFolder, String fileName, InputStream content, OverwriteAction overwriteAction ) {
        DocumentFile parent = getFile(parentFolder);
        if (parent != null && parent.canWrite()) {
            DocumentFile existing = parent.findFile(fileName);
            if (existing != null && existing.exists()) {
                if (overwriteAction == OverwriteAction.FAIL) {
                    throw new IllegalArgumentException("File "+ fileName + " already exists in folder " + parentFolder.getUri());
                }
                if (overwriteAction == OverwriteAction.SKIP_SILENT) {
                    return false;
                }
            }
            DocumentFile newFile = parent.createFile("application/octet-stream", fileName);
            if (newFile != null) {
                Uri newFileUri = newFile.getUri();

                try (OutputStream out = context.getContentResolver().openOutputStream(newFileUri)) {
                    if (out != null) {
                        IOUtil.copy(content, out);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to write to file: " + fileName + " in folder " + parentFolder.getUri(), e);
                }
            }
            return true;
        } else {
            throw new RuntimeException("No write permission or folder does not exist for folder: " + parentFolder.getUri());
        }
    }

    @Nullable
    private DocumentFile getFile(FilesystemUri uri) {
        if (uri.isFolder()) {
            return DocumentFile.fromTreeUri(context, uri.getUri());
        }
        return  DocumentFile.fromSingleUri(context,uri.getUri());
    }

    public Date getModificationDate(FilesystemUri uri) {
        try {
            DocumentFile file = getFile( uri);
            return file == null ? new Date(0) : new Date(file.lastModified());
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
