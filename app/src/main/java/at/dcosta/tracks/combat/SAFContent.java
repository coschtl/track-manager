package at.dcosta.tracks.combat;

import android.content.Context;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import at.dcosta.tracks.track.file.FileLocator;
import at.dcosta.tracks.track.file.SAFFileLocator;

public class SAFContent implements Content {

    private final Context context;
    private final Uri uri;
    private final Date modificationDate;

    public SAFContent(Context context, Uri uri, Date modificationDate) {
        this.context = context;
        this.uri = uri;
        this.modificationDate = modificationDate;
    }

    @Override
    public String getFullPath() {
        return uri.toString();
    }

    @Override
    public String getName() {
        return FileLocator.getFileNameFromPath(uri.toString(), SAFFileLocator.PATH_SEPARATORS);
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
