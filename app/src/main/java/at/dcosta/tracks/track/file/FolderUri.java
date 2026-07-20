package at.dcosta.tracks.track.file;

import android.net.Uri;

public class FolderUri extends FilesystemUri {

    public FolderUri(Uri uri) {
        super(uri, true);
    }

    public FolderUri(String fullPath) {
        this(Uri.parse(fullPath));
    }
}
