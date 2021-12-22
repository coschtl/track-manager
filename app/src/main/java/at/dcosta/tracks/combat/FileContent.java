package at.dcosta.tracks.combat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileContent implements Content {

    private final File file;

    public FileContent(File file) {
        this.file = file;
    }

    @Override
    public String getFullPath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public InputStream getInputStream() {
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
