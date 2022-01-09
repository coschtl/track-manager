package at.dcosta.tracks.combat;

import java.io.InputStream;
import java.util.Date;

public interface Content {

    String getFullPath();

    String getName();

    Date getModificationDate();

    InputStream getInputStream();
}
