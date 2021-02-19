package at.dcosta.android.fw.props;

import java.io.File;
import java.net.URI;

public class Folder extends File {

    private static final long serialVersionUID = 1L;

    private Folder(File dir, String name) {
        super(dir, name);
    }

    private Folder(String path) {
        super(path);
    }

    private Folder(String dirPath, String name) {
        super(dirPath, name);
    }

    private Folder(URI uri) {
        super(uri);
    }

}
