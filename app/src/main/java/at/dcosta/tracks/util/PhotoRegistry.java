package at.dcosta.tracks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.TreeSet;

import at.dcosta.android.fw.IOUtil;

public class PhotoRegistry {

	private final File photoRegistry;
	private Set<String> photos;
	private boolean persisted;

	public PhotoRegistry() {
		Configuration config = Configuration.getInstance();
		photoRegistry = new File(config.getWorkingDir() + "/photos.dat");
		loadPhotos();
	}

	public PhotoRegistry add(String path) {
		photos.add(path);
		persisted = false;
		return this;
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
	private void loadPhotos() {
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new FileInputStream(photoRegistry));
			photos = (Set<String>) oin.readObject();
		} catch (Exception e) {
			photos = new TreeSet<String>();
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
