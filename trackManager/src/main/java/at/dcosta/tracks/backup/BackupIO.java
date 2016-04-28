package at.dcosta.tracks.backup;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.tracks.db.SavedSearchesDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.util.SavedSearch;

public class BackupIO {

	private static final String KEY_DB_TRACKS = "dbTracks";
	private static final String KEY_DB_PROPERTIES = "dbProperties";
	private static final String KEY_DB_SEARCHES = "dbSearches";

	private final TrackDbAdapter trackDbAdapter;
	private final PropertyDbAdapter propertyDbAdapter;
	private final SavedSearchesDbAdapter savedSearchesDbAdapter;

	public BackupIO(TrackDbAdapter trackDbAdapter, PropertyDbAdapter propertyDbAdapter, SavedSearchesDbAdapter savedSearchesDbAdapter) {
		this.trackDbAdapter = trackDbAdapter;
		this.propertyDbAdapter = propertyDbAdapter;
		this.savedSearchesDbAdapter = savedSearchesDbAdapter;
	}

	public final void backup(String filename) throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(KEY_DB_TRACKS, serializeTrackDb());
		data.put(KEY_DB_PROPERTIES, serializePropertyDb());
		data.put(KEY_DB_SEARCHES, serializeSearchDb());

		try {
			writeData(data, filename);
		} catch (ClassNotFoundException e) {
			throw new IOException("Serialization not possible: " + e.getMessage());
		}
	}

	private void deserializePropertyDb(List<Property> l) {
		propertyDbAdapter.clear();
		for (Property p : l) {
			propertyDbAdapter.createPropertyEntry(p);
		}
	}

	private void deserializeSearchDb(List<SavedSearch> l) {
		savedSearchesDbAdapter.clear();
		for (SavedSearch s : l) {
			savedSearchesDbAdapter.add(s);
		}
	}

	private void deserializeTrackDb(List<TrackDescription> l) {
		trackDbAdapter.clear();
		for (TrackDescription td : l) {
			trackDbAdapter.createEntry(td);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readData(String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(filename));
			return (Map<String, Object>) in.readObject();
		} finally {
			IOUtil.close(in);
		}
	}

	@SuppressWarnings("unchecked")
	public final void restore(String filename) throws IOException {
		try {
			Map<String, Object> data = readData(filename);
			deserializePropertyDb((List<Property>) data.get(KEY_DB_PROPERTIES));
			deserializeTrackDb((List<TrackDescription>) data.get(KEY_DB_TRACKS));
			deserializeSearchDb((List<SavedSearch>) data.get(KEY_DB_SEARCHES));
		} catch (ClassNotFoundException e) {
			throw new IOException("Serialization not possible: " + e.getMessage());
		}
	}

	private List<Property> serializePropertyDb() {
		Iterator<Property> all = propertyDbAdapter.fetchAllProperties();
		List<Property> l = new ArrayList<Property>();
		while (all.hasNext()) {
			l.add(all.next());
		}
		return l;
	}

	private List<SavedSearch> serializeSearchDb() {
		Iterator<SavedSearch> all = savedSearchesDbAdapter.fetchAllEntries();
		List<SavedSearch> l = new ArrayList<SavedSearch>();
		while (all.hasNext()) {
			l.add(all.next());
		}
		return l;
	}

	private List<TrackDescription> serializeTrackDb() {
		Iterator<TrackDescription> all = trackDbAdapter.fetchAllEntries(true);
		List<TrackDescription> l = new ArrayList<TrackDescription>();
		while (all.hasNext()) {
			l.add(all.next());
		}
		return l;
	}

	private void writeData(Map<String, Object> data, String filename) throws IOException, ClassNotFoundException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(data);
		} finally {
			IOUtil.close(out);
		}
	}
}
