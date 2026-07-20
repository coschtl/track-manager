package at.dcosta.tracks.track.file;

import android.net.Uri;

public class FileUri extends FilesystemUri {

    public FileUri(Uri uri) {
        super(uri, false);
    }

    public FileUri(String fullPath) {
        this(Uri.parse(fullPath));
    }
}
