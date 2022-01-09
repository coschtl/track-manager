package at.dcosta.tracks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import at.dcosta.android.fw.IOUtil;

public class PhotoRegistry {

    private final File photoRegistry;
    private Set<Photo> photos;
    private Date latestCreateDate;
    private boolean persisted;

    public static void clearRegistry() {
        new PhotoRegistry(false).clear().persist();
    }

    public PhotoRegistry() {
        this(true);
    }

    public PhotoRegistry(boolean loadPhotos) {
        Configuration config = Configuration.getInstance();
        photoRegistry = new File(config.getWorkingDir() + "/photos.dat");
        if (loadPhotos) {
            loadCache();
        } else {
            photos = new TreeSet<Photo>();
        }
    }

    public PhotoRegistry add(Photo path) {
        photos.add(path);
        persisted = false;
        return this;
    }

    public long getLatestCreateDate() {
        if (latestCreateDate == null) {
            latestCreateDate = new Date(0);
            photos.forEach(photo -> {
                if (latestCreateDate.getTime() < photo.getCreatedOn()) {
                    latestCreateDate = new Date(photo.getCreatedOn());
                }
            });
        }
        return latestCreateDate.getTime();
    }

    public PhotoRegistry clear() {
        photos.clear();
        persisted = false;
        return this;
    }

    public boolean contains(String path) {
        return photos.contains(path);
    }

    @SuppressWarnings("unchecked")
    private void loadCache() {
        photos = new TreeSet<Photo>();
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new FileInputStream(photoRegistry));
            Set<?> s = (Set<?>) oin.readObject();
            if (!s.isEmpty()) {
                if (s.iterator().next() instanceof Photo) {
                    photos.addAll((Collection<? extends Photo>) s);
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            IOUtil.close(oin);
        }
    }

    public PhotoRegistry persist() {
        if (!persisted) {
            ObjectOutputStream oout = null;
            try {
                photoRegistry.delete();
                photoRegistry.createNewFile();
                oout = new ObjectOutputStream(new FileOutputStream(photoRegistry));
                oout.writeObject(photos);
                oout.flush();
                persisted = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                IOUtil.close(oout);
            }
        }
        return this;
    }
}
