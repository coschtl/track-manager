package at.dcosta.tracks.backup;

import android.app.Activity;
import android.widget.ProgressBar;

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
import java.util.logging.Logger;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.tracks.CombatFactory;
import at.dcosta.tracks.TrackEdit;
import at.dcosta.tracks.TrackManager;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.db.SavedSearchesDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.FileLocator;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.SavedSearch;

public class BackupIO {

    private static final Logger LOGGER = Logger.getLogger(BackupIO.class.getName());

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

    public final void backup(String filename, ProgressBar progressBar) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_DB_TRACKS, serializeTrackDb( progressBar));
        data.put(KEY_DB_PROPERTIES, serializePropertyDb());
        data.put(KEY_DB_SEARCHES, serializeSearchDb());
        writeData(data, filename);
    }

    private void deserializePropertyDb(List<Property> l) {
        List<Property> toRestore = new ArrayList<>();
        propertyDbAdapter.fetchAllProperties(Configuration.PROPERTY_WORKING_DIR).forEachRemaining(toRestore::add);
        propertyDbAdapter.fetchAllProperties(Configuration.PROPERTY_TRACK_FOLDER).forEachRemaining(toRestore::add);
        propertyDbAdapter.fetchAllProperties(Configuration.PROPERTY_WORKING_DIR).forEachRemaining(toRestore::add);
        propertyDbAdapter.clear();
        for (Property p : l) {
            if (p != null) {
                propertyDbAdapter.createPropertyEntry(p);
            }
        }
        for (Property p : toRestore) {
            do {
            } while (propertyDbAdapter.deleteProperty(p.getName()));
            propertyDbAdapter.createPropertyEntry(p);
        }
    }

    private void deserializeSearchDb(List<SavedSearch> l) {
        savedSearchesDbAdapter.clear();
        for (SavedSearch s : l) {
            if (s != null) {
                savedSearchesDbAdapter.add(s);
            }
        }
    }

    private void deserializeTrackDb(List<TrackDescriptionNG> l, Activity activity, ProgressBar progressBar) {
        trackDbAdapter.clear();
        ActivityFactory activityFactory = new ActivityFactory(activity);
        final float increment = (100f / (float) l.size());
        float totalInc = 0f;

        for (Object td : l) {
            TrackDescriptionNG tdng = td instanceof TrackDescriptionNG ? (TrackDescriptionNG) td : new TrackDescriptionNG((TrackDescription) td, activityFactory);
            FileLocator fileLocator = CombatFactory.getFileLocator(activity);
            Content track = fileLocator.findTrack(tdng.getPath());
            if (track == null) {
                LOGGER.severe("can not find track for " + tdng.getPath());
                continue;
            }
            tdng.setPath(track.getFullPath());
            String iconExtra = tdng.getSingleValueExtra("icon");
            if (iconExtra != null && iconExtra.startsWith("res/")) {
                iconExtra = iconExtra.replace("/drawable-hdpi-v4/", "/mipmap-hdpi/");
                tdng.setSingleValueExtra("icon", iconExtra);
            }
            TrackEdit.updateTrack(TrackManager.context(), tdng, iconExtra, activityFactory);
            trackDbAdapter.createEntry(tdng);
            totalInc += increment;
            progressBar.setProgress((int) totalInc);
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
    public final void restore(String filename, Activity activity, ProgressBar progressBar) throws IOException {
        try {
            System.out.println("loading data from file...");
            Map<String, Object> data = readData(filename);
            System.out.println("restoring properties...");
            deserializePropertyDb((List<Property>) data.get(KEY_DB_PROPERTIES));
            System.out.println("restoring tracks...");
            deserializeTrackDb((List<TrackDescriptionNG>) data.get(KEY_DB_TRACKS), activity, progressBar);
            System.out.println("restoring searches...");
            deserializeSearchDb((List<SavedSearch>) data.get(KEY_DB_SEARCHES));
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization not possible: " + e.getMessage());
        }
    }

    private List<Property> serializePropertyDb() {
        Iterator<Property> all = propertyDbAdapter.fetchAllProperties();
        List<Property> l = new ArrayList<>();
        while (all.hasNext()) {
            l.add(all.next());
        }
        return l;
    }

    private List<SavedSearch> serializeSearchDb() {
        Iterator<SavedSearch> all = savedSearchesDbAdapter.fetchAllEntries();
        List<SavedSearch> l = new ArrayList<>();
        while (all.hasNext()) {
            l.add(all.next());
        }
        return l;
    }

    private List<TrackDescriptionNG> serializeTrackDb( ProgressBar progressBar) {
        final float increment = (100f / (float) trackDbAdapter.countAllEntries(false));
        float totalInc = 0f;
        Iterator<TrackDescriptionNG> all = trackDbAdapter.fetchAllEntries(true);
        List<TrackDescriptionNG> l = new ArrayList<>();
        while (all.hasNext()) {
            l.add(all.next());
            totalInc += increment;
            progressBar.setProgress((int) totalInc);
        }
        return l;
    }

    private void writeData(Map<String, Object> data, String filename) throws IOException {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(data);
        } finally {
            IOUtil.close(out);
        }
    }
}
