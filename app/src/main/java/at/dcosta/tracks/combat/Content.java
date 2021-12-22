package at.dcosta.tracks.combat;

import java.io.InputStream;

public interface Content {
    String getFullPath();

    String getName();

    InputStream getInputStream();
}
