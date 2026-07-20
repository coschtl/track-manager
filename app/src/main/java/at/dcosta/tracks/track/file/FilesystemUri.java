package at.dcosta.tracks.track.file;

import android.net.Uri;
import android.support.annotation.NonNull;

public abstract class FilesystemUri {
    private final Uri uri;
    private final boolean isFolder;

    public FilesystemUri(Uri uri, boolean isFolder) {
        this.uri = uri;
        this.isFolder = isFolder;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public Uri getUri() {
        return uri;
    }

    @NonNull
    @Override
    public String toString() {
        return getUri() == null ? "null" : getUri().toString();
    }
}
