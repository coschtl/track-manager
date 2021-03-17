package at.dcosta.tracks.util;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.android.fw.props.ConfigurationException;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyConfiguration;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.tracks.R;
import at.dcosta.tracks.RecordTrack;
import at.dcosta.tracks.RecordTrackOsm;
import at.dcosta.tracks.ServerProperties;
import at.dcosta.tracks.TrackOnMap;
import at.dcosta.tracks.TrackOnOsmMap;
import at.dcosta.tracks.db.DatabaseHelper;

public class Configuration {

    public static final String AVAILABLE_PROPS_XML = "at/dcosta/tracks/availableProps.xml";
    public static final String PROPERTY_TRACK_FOLDER = "trackFolder";
    public static final String PROPERTY_PHOTO_FOLDER = "photoFolder";
    public static final String PROPERTY_TRACKING_SERVER = "trackingServer";
    public static final String PROPERTY_SERVER_SAVE_PATH = "serverSavePath";
    public static final String PROPERTY_SERVER_CLOSE_PATH = "serverClosePath";
    public static final String PROPERTY_TRACKING_PROTOCOL = "trackingProtocol";
    public static final String PROPERTY_CUSTOM = "customProperty";
    public static final int STATUS_READONLY = -1;
    public static final int STATUS_MODIFY_VALUE = 0;
    public static final int STATUS_DELETABLE = 1;
    public static final int STATUS_EDITABLE = 2;
    // xxx
    public static final String TYPE_FOLDER = "folder";
    public static final String TYPE_PROPERTY = "property";
    private static final String PROPERTY_WORKING_DIR = "workingDir";
    private static final String RECORDED_TRACKS_SUBDIR = "recordedTracks";
    private static final String COPIED_TRACKS_SUBDIR = "copiedTracks";
    private static Configuration _instance;
    private final SimpleDateFormat dateFormat;
    private final int wipeSensitivity, track_foto_tolerance;
    private final Map<String, Property> singleValues = new HashMap<String, Property>();
    private final Map<String, List<Property>> multiValues = new HashMap<String, List<Property>>();
    private final SQLiteOpenHelper databaseHelper;
    private final PropertyDbAdapter propertyDbAdapter;
    private TrackCache trackCache;

    private Configuration(Context ctx) {
        databaseHelper = new DatabaseHelper(ctx);
        dateFormat = new SimpleDateFormat(ctx.getString(R.string.CFG_DATE_FORMAT));
        wipeSensitivity = Integer.parseInt(ctx.getString(R.string.CFG_WIPE_SENSITIVITY));
        track_foto_tolerance = Integer.parseInt(ctx.getString(R.string.CFG_TRACK_FOTO_TOLERANCE_MINUTES));

        InputStream is = getClass().getClassLoader().getResourceAsStream(AVAILABLE_PROPS_XML);
        PropertyConfiguration cfg = new PropertyConfiguration(is);
        IOUtil.close(is);

        propertyDbAdapter = new PropertyDbAdapter(ctx, cfg);
        // propertyDbAdapter.forceUpgrade(7);
        // propertyDbAdapter.clear();
        propertyDbAdapter.assurePropertiesInDb();
        Iterator<Property> it = propertyDbAdapter.fetchAllProperties();
        while (it.hasNext()) {
            Property prop = it.next();
            String name = prop.getName();
            if (prop.isMultivalue()) {
                List<Property> l;
                if (multiValues.containsKey(name)) {
                    l = multiValues.get(name);
                } else {
                    l = new ArrayList<Property>();
                }
                l.add(prop);
                multiValues.put(name, l);
            } else {
                singleValues.put(name, prop);
            }
        }
        propertyDbAdapter.close();
    }

    public static synchronized Configuration getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("The configuration has not been initialized yet!");
        }
        return _instance;
    }

    public static synchronized Configuration getInstance(Context ctx) {
        System.out.println("---------------------------------------");
        System.out.println(" init Configuration ");
        System.out.println("---------------------------------------");
        if (_instance == null) {
            System.out.println("Creating new");
            // Check whether this app has write external storage permission or not.
            //int writeExternalStoragePermission = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            // If do not grant write external storage permission.
            //if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            //	ActivityCompat.requestPermissions((Activity) ctx, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            //}
            _instance = new Configuration(ctx);
        }
        return _instance;
    }

    public File getCopiedTracksDir() {
        return new File(getWorkingDir(), COPIED_TRACKS_SUBDIR);
    }

    public SQLiteOpenHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public Class<?> getMapViewClass() {
        if (useOsm()) {
            return TrackOnOsmMap.class;
        }
        return TrackOnMap.class;
    }

    public List<Property> getMultiValueDbProperty(String type) {
        return multiValues.get(type);
    }

    public PropertyDbAdapter getPropertyDbAdapter() {
        return propertyDbAdapter;
    }

    public File getRecordedTracksDir() {
        return new File(getWorkingDir(), RECORDED_TRACKS_SUBDIR);
    }

    public Class<?> getRecordTrackClass() {
        if (useOsm()) {
            return RecordTrackOsm.class;
        }
        return RecordTrack.class;
    }

    public ServerProperties getServerProperties() {
        ServerProperties sp = new ServerProperties();
        sp.setServer(getSingleValueDbProperty(PROPERTY_TRACKING_SERVER).getValue());
        sp.setSavePath(getSingleValueDbProperty(PROPERTY_SERVER_SAVE_PATH).getValue());
        sp.setClosePath(getSingleValueDbProperty(PROPERTY_SERVER_CLOSE_PATH).getValue());
        sp.setTrackingProtocol(getSingleValueDbProperty(PROPERTY_TRACKING_PROTOCOL).getValue());
        sp.initialize();
        return sp;
    }

    public Property getSingleValueDbProperty(String type) {
        return singleValues.get(type);
    }

    public int getTrack_foto_tolerance() {
        return track_foto_tolerance;
    }

    public TrackCache getTrackCache() {
        return trackCache;
    }

    public int getWipeSensitivity() {
        return wipeSensitivity;
    }

    public String getWorkingDir() {
        return getSingleValueDbProperty(PROPERTY_WORKING_DIR).getValue();
    }

    public void setWorkingDir(String workingDir) {
        Property prop = getSingleValueDbProperty(PROPERTY_WORKING_DIR);
        prop.setValue(workingDir);
        propertyDbAdapter.updateProperty(prop);
        trackCache = new TrackCache();
    }

    public void updateSingleValueProperty(String name, String value) {
        Property prop = getSingleValueDbProperty(name);
        if (prop == null) {
            throw new ConfigurationException("Unknown property: '" + name + "'!");
        }
        prop.setValue(value);
        propertyDbAdapter.updateProperty(prop);
    }

    public boolean useOsm() {
        return "OSM".equals(getSingleValueDbProperty("mapType").getValue());
    }

    public boolean isMale() {
        return "Male".equals(getSingleValueDbProperty("sex").getValue());
    }

    public Date getBirthday() {
        try {
            Property birthday = getSingleValueDbProperty("birthday");
            if (birthday.getValue() == null) {
                return null;
            }
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            return df.parse(birthday.getValue());
        } catch (ParseException e) {
            return null;
        }
    }

}
