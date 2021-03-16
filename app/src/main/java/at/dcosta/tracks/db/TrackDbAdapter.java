package at.dcosta.tracks.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.NameValuePair;
import at.dcosta.android.fw.db.AbstractCursorIterator;
import at.dcosta.android.fw.db.AbstractDbAdapter;
import at.dcosta.android.fw.db.DbUtil;
import at.dcosta.android.fw.props.Property;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;

/**
 * Simple property database access helper class.
 * <p>
 * This has been improved from the first version of this tutorial through the addition of better error handling and also using returning a Cursor instead of
 * using a collection of inner classes (which is less scalable and not recommended).
 */
public class TrackDbAdapter extends AbstractDbAdapter {

    private final ActivityFactory activityFactory;

    /**
     * Constructor - takes the context to allow the database to be opened/created
     */
    public TrackDbAdapter(SQLiteOpenHelper databaseHelper, Context context) {
        super(databaseHelper);
        activityFactory = new ActivityFactory(context);
    }

    private static List<ContentValues> extrasToContentValues(TrackDescriptionNG descr, long trackId) {
        List<ContentValues> cvs = new ArrayList<ContentValues>();
        Iterator<Entry<String, String>> single = descr.getSingleValueExtras().entrySet().iterator();
        if (trackId < 0) {
            trackId = descr.getId();
        }
        while (single.hasNext()) {
            Entry<String, String> extra = single.next();
            String value = extra.getValue();
            if (value != null) {
                ContentValues cv = new ContentValues();
                cv.put(DB_EXT.COL_TRACK_ID, trackId);
                cv.put(DB_EXT.COL_NAME, extra.getKey());
                cv.put(DB_EXT.COL_VALUE, value);
                cv.put(DB_EXT.COL_MULTIVALUE, 0);
                cvs.add(cv);
            }
        }
        Iterator<Entry<String, List<String>>> multi = descr.getMultiValueExtras().entrySet().iterator();
        while (multi.hasNext()) {
            Entry<String, List<String>> extra = multi.next();
            List<String> values = extra.getValue();
            if (values != null) {
                for (String value : values) {
                    ContentValues cv = new ContentValues();
                    cv.put(DB_EXT.COL_TRACK_ID, trackId);
                    cv.put(DB_EXT.COL_NAME, extra.getKey());
                    cv.put(DB_EXT.COL_VALUE, value);
                    cv.put(DB_EXT.COL_MULTIVALUE, 1);
                    cvs.add(cv);
                }
            }
        }
        return cvs;
    }

    private static final ContentValues toContentValues(TrackDescriptionNG descr) {
        ContentValues cv = new ContentValues();
        if (descr.getId() > 0) {
            cv.put(DB.COL_ID, descr.getId());
        }
        cv.put(DB.COL_NAME, descr.getName());
        cv.put(DB.COL_PATH, descr.getPath());
        cv.put(DB.COL_START_TIME, DateUtil.getEpochSecs(descr.getStartTime()));
        cv.put(DB.COL_END_TIME, DateUtil.getEpochSecs(descr.getEndTime()));
        cv.put(DB.COL_MOVING_TIME, descr.getMovingTimeSeconds());
        cv.put(DB.COL_DIST_H, descr.getHorizontalDistance());
        cv.put(DB.COL_DIST_V_UP, descr.getVerticalUp());
        cv.put(DB.COL_AVG_PULSE, descr.getAvgPulse());
        cv.put(DB.COL_MAX_PULSE, descr.getMaxPulse());
        return cv;
    }

    public int clear() {
        return db().delete(DB.DATABASE_TABLE, DB.COL_STATUS + ">0", null);
        // return db().delete(DB.DATABASE_TABLE, DB.COL_PATH + " not like 'copy%'", null);
    }

    public long copyEntry(Long trackId, String nameOfCopy, String pathOfCopy, long microsecondsAfterOriginal) {
        return copyEntry(fetchEntry(trackId), nameOfCopy, pathOfCopy, microsecondsAfterOriginal);
    }

    public long copyEntry(TrackDescriptionNG orig, String nameOfCopy, String pathOfCopy, long microsecondsAfterOriginal) {
        TrackDescriptionNG copy = new TrackDescriptionNG(-1l, nameOfCopy, pathOfCopy, (orig.getStartTime().getTime() + microsecondsAfterOriginal) / 1000l, (orig
                .getEndTime().getTime() + microsecondsAfterOriginal) / 1000l, orig.getMovingTimeSeconds(), orig.getHorizontalDistance(), orig.getVerticalUp(), orig.getAvgPulse(), orig.getMaxPulse(),
                activityFactory);
        Iterator<Entry<String, String>> it = orig.getSingleValueExtras().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> extra = it.next();
            copy.setSingleValueExtra(extra.getKey(), extra.getValue());
        }
        Iterator<Entry<String, List<String>>> iter = orig.getMultiValueExtras().entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, List<String>> extra = iter.next();
            if (!TrackDescriptionNG.EXTRA_PHOTO.equals(extra.getKey())) {
                copy.setMultiValueExtra(extra.getKey(), extra.getValue());
            }
        }
        copy.setSingleValueExtra(DB.COL_STATUS, "0");
        return createEntry(copy);
    }

    private String correctApemapModifiedPath(String path, File pathDir) {
        boolean corrected = false;
        while (!pathDir.exists()) {
            pathDir = pathDir.getParentFile();
            if (pathDir == null) {
                Log.w("TrackdbAdapter", "can not get a new name for path '" + path + "' (pathdir is null)");
                return null;
            }
            corrected = true;
        }
        String pathNoDirAndHash = path.substring(path.lastIndexOf(File.separatorChar) + 1);
        if (corrected) {
            return new File(pathDir, pathNoDirAndHash).getAbsolutePath();
        }
        int pos = pathNoDirAndHash.lastIndexOf('.');
        if (pos == -1) {
            pathNoDirAndHash += "#";
        } else {
            pathNoDirAndHash = pathNoDirAndHash.substring(0, pos) + "#";
        }
        for (String file : pathDir.list()) {
            if (file.indexOf(pathNoDirAndHash) != -1) {
                path = new File(pathDir, file).getAbsolutePath();
                return path;
            }
        }
        Log.w("TrackdbAdapter", "can not get a new name for path '" + path + "'");
        return path;
    }

    public long createEntry(TrackDescriptionNG trackDescription) {
        ContentValues track = toContentValues(trackDescription);
        track.put(DB.COL_STATUS, "1");
        long id = db().insert(DB.DATABASE_TABLE, null, track);
        List<ContentValues> ce = extrasToContentValues(trackDescription, id);
        for (ContentValues cv : ce) {
            db().insert(DB_EXT.DATABASE_TABLE, null, cv);
        }
        return id;
    }

    private Set<String> createSet(Cursor cursor, final int colId) {
        Set<String> s = new HashSet<String>();
        AbstractCursorIterator<String> it = new AbstractCursorIterator<String>(cursor) {
            @Override
            public String createObject() {
                return cursor.getString(colId);
            }
        };
        while (it.hasNext()) {
            s.add(it.next());
        }
        return s;
    }

    private final TrackDescriptionNG createTrackDescription(Cursor cursor) {
        return createTrackDescription(cursor, true);
    }

    private final TrackDescriptionNG createTrackDescription(Cursor cursor, boolean closeCursor) {
        if (cursor != null && !cursor.isClosed()) {
            // look if path is still valid
            String dbPath = cursor.getString(DB.COL_MAPPING.get(DB.COL_PATH));
            String path = validatePath(dbPath);

            TrackDescriptionNG td = new TrackDescriptionNG(cursor.getLong(DB.COL_MAPPING.get(DB.COL_ID)), cursor.getString(DB.COL_MAPPING.get(DB.COL_NAME)), path,
                    cursor.getLong(DB.COL_MAPPING.get(DB.COL_START_TIME)), cursor.getLong(DB.COL_MAPPING.get(DB.COL_END_TIME)), cursor.getInt(DB.COL_MAPPING
                    .get(DB.COL_MOVING_TIME)), cursor.getLong(DB.COL_MAPPING.get(DB.COL_DIST_H)),
                    cursor.getInt(DB.COL_MAPPING.get(DB.COL_DIST_V_UP)), cursor.getInt(DB.COL_MAPPING.get(DB.COL_AVG_PULSE)), cursor.getInt(DB.COL_MAPPING.get(DB.COL_MAX_PULSE)), activityFactory);
            if (closeCursor) {
                DbUtil.close(cursor);
            }
            Cursor ce = fetchExtras(td.getId(), false);
            ExtrasIterator it = new ExtrasIterator(ce);
            while (it.hasNext()) {
                NameValuePair nvp = it.next();
                td.setSingleValueExtra(nvp.getName(), nvp.getValue());
            }
            ce = fetchExtras(td.getId(), true);
            it = new ExtrasIterator(ce);
            while (it.hasNext()) {
                NameValuePair nvp = it.next();
                td.addMultiValueExtra(nvp.getName(), nvp.getValue());
            }
            if (!dbPath.equals(path)) {
                updateEntry(td);
            }
            return td;
        }
        return null;
    }

    public int deleteAllExtras(String type) {
        return db().delete(DB_EXT.DATABASE_TABLE, DB_EXT.COL_NAME + "=?", new String[]{type});
    }

    /**
     * Delete the property with the given rowId
     *
     * @param rowId id of property to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEntry(long rowId) {
        String idString = Long.toString(rowId);
        ContentValues cv = new ContentValues();
        cv.put(DB.COL_STATUS, -1);
        boolean success = db().update(DB.DATABASE_TABLE, cv, DB.COL_ID + "=?", new String[]{idString}) > 0;
        if (success) {
            db().delete(DB_EXT.DATABASE_TABLE, DB_EXT.COL_TRACK_ID + " =?", new String[]{idString});
        }
        return success;
    }

    public boolean extraIsInDb(String type, String value) {
        Cursor cursor = db().query(true, DB_EXT.DATABASE_TABLE, new String[]{DB_EXT.COL_TRACK_ID}, DB_EXT.COL_NAME + "=? and " + DB_EXT.COL_VALUE + "=?",
                new String[]{type, value}, null, null, null, null);
        boolean inDb = cursor.moveToFirst();
        DbUtil.close(cursor);
        return inDb;
    }

    public Iterator<TrackDescriptionNG> fetchAllEntries(boolean includeDeleted) {
        return new TrackIterator(fetchAllEntriesAsCursor(includeDeleted), this);
    }

    /**
     * Return a Cursor over the list of all properties in the database
     *
     * @return Cursor over all properties
     */
    public Cursor fetchAllEntriesAsCursor(boolean includeDeleted) {
        String where = includeDeleted ? "" : DB.COL_STATUS + ">=0";
        return db().query(DB.DATABASE_TABLE, DB.COL_NAMES, where, null, null, null, DB.COL_START_TIME);
    }

    public long countAllEntries(boolean includeDeleted) {
        if (includeDeleted) {
            return DatabaseUtils.queryNumEntries(db(), DB.DATABASE_TABLE);
        }
        return DatabaseUtils.queryNumEntries(db(), DB.DATABASE_TABLE, DB.COL_STATUS + ">=0");
    }

    public TrackDescriptionNG fetchEntry(long rowId) throws SQLException {
        return createTrackDescription(fetchEntryAsCursor(rowId));
    }

    /**
     * Return a Cursor positioned at the property that matches the given rowId
     *
     * @param rowId id of property to retrieve
     * @return Cursor positioned to matching property, if found
     * @throws SQLException if property could not be found/retrieved
     */
    public Cursor fetchEntryAsCursor(long rowId) throws SQLException {

        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_ID + "=?", new String[]{Long.toString(rowId)}, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor;
            }
        }
        return null;
    }

    private Cursor fetchExtras(long rowId, boolean multivalue) throws SQLException {
        String multivalueValue = multivalue ? "1" : "0";
        return db().query(true, DB_EXT.DATABASE_TABLE, DB_EXT.COL_NAMES, DB_EXT.COL_TRACK_ID + "=?" + " and " + DB_EXT.COL_MULTIVALUE + "=?",
                new String[]{Long.toString(rowId), multivalueValue}, null, null, null, null);
    }

    public Iterator<TrackDescriptionNG> findEntries(Date start, Date end) throws SQLException {
        if (start == null || end == null || start.after(end)) {
            return AbstractCursorIterator.emptyIterator();
        }
        String where = new StringBuilder().append(DB.COL_START_TIME).append(">=? and ").append(DB.COL_END_TIME).append("<=?").append("and ")
                .append(DB.COL_STATUS).append(">=0").toString();
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, where,
                new String[]{Long.toString(DateUtil.getEpochSecs(start)), Long.toString(DateUtil.getEpochSecs(end))}, null, null,
                DB.COL_START_TIME + " ASC ", null);
        return new TrackIterator(cursor, this);
    }

    public Iterator<TrackDescriptionNG> findEntries(Date start, Date end, final String activity, String nameLike, String commentLike) throws SQLException {
        List<Object> args = new ArrayList<Object>();
        StringBuilder where = new StringBuilder();
        if (start != null) {
            where.append(DB.COL_START_TIME).append(">=?");
            args.add(Long.toString(DateUtil.getEpochSecs(DateUtil.getDayStart(start))));
        }
        if (end != null) {
            if (where.length() > 0) {
                where.append(" and ");
            }
            where.append(DB.COL_START_TIME).append("<=?");
            args.add(Long.toString(DateUtil.getEpochSecs(DateUtil.getDayEnd(end))));
        }
        if (nameLike != null) {
            if (where.length() > 0) {
                where.append(" and ");
            }
            where.append(DB.COL_NAME).append(" like ?");
            args.add(new StringBuilder("%").append(nameLike).append("%").toString());
        }
        if (commentLike != null) {
            String selection = new StringBuilder(DB_EXT.COL_NAME)
                    .append("='")
                    .append(TrackDescriptionNG.EXTRA_COMMENT)
                    .append("' and ")
                    .append(DB_EXT.COL_VALUE)
                    .append(" like '%")
                    .append(commentLike)
                    .append("%'")
                    .toString();
            Cursor trackIds = db().query(DB_EXT.DATABASE_TABLE, new String[]{DB_EXT.COL_TRACK_ID}, selection, null, null, null, null);
            if (trackIds.moveToFirst()) {
                if (where.length() > 0) {
                    where.append(" and ");
                }
                where.append(DB.COL_ID).append(" in (?");
                args.add(trackIds.getString(0));
                while (trackIds.moveToNext()) {
                    where.append(", ?");
                    args.add(trackIds.getString(0));
                }
                where.append(")");
            }
            trackIds.close();
        }
        if (where.length() == 0 && activity == null) {
            // return AbstractCursorItertaor.emptyIterator();
        }
        if (where.length() > 0) {
            where.append(" and ");
        }
        where.append(DB.COL_STATUS).append(">=0");
        Cursor cursor;
        if (args.size() == 0) {
            cursor = fetchAllEntriesAsCursor(false);
        } else {
            cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, where.toString(), args.toArray(new String[args.size()]), null, null,
                    DB.COL_START_TIME + " ASC ", null);
        }

        final TrackIterator trackIterator = new TrackIterator(cursor, this);
        return new Iterator<TrackDescriptionNG>() {
            private TrackDescriptionNG akt;
            private boolean nextCalled = false;

            @Override
            public boolean hasNext() {
                if (nextCalled) {
                    akt = null;
                    nextCalled = false;
                }
                while (trackIterator.hasNext() && akt == null) {
                    TrackDescriptionNG next = trackIterator.next();
                    String icon = next.getSingleValueExtra(TrackDescriptionNG.EXTRA_ICON);
                    if (activity == null || icon != null && activity.equals(icon)) {
                        akt = next;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public TrackDescriptionNG next() {
                nextCalled = true;
                return akt;
            }

            @Override
            public void remove() {
                trackIterator.remove();
            }
        };
    }

    public TrackDescriptionNG findEntry(Date d) throws SQLException {
        if (d == null) {
            return null;
        }
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_START_TIME + "=?", new String[]{Long.toString(d.getTime() / 1000l)}, null,
                null, null, "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrackDescription(cursor);
                }
            } finally {
                DbUtil.close(cursor);
            }
        }
        return null;
    }

    public TrackDescriptionNG findEntryByPath(String path) throws SQLException {
        if (path == null) {
            return null;
        }
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_PATH + "=?", new String[]{path}, null, null, null, "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrackDescription(cursor);
                }
            } finally {
                DbUtil.close(cursor);
            }
        }
        return null;
    }

    public TrackDescriptionNG findEntryLikePath(String path) throws SQLException {
        if (path == null) {
            return null;
        }
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_PATH + " like ?", new String[]{path}, null, null, null, "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrackDescription(cursor);
                }
            } finally {
                DbUtil.close(cursor);
            }
        }
        return null;
    }

    public TrackDescriptionNG findNextEntry(Date d) throws SQLException {
        if (d == null) {
            return null;
        }
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_START_TIME + "> ? and " + DB.COL_STATUS + ">=0",
                new String[]{Long.toString(d.getTime() / 1000l)}, null, null, DB.COL_START_TIME, "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrackDescription(cursor);
                }
            } finally {
                DbUtil.close(cursor);
            }
        }
        return null;
    }

    public TrackDescriptionNG findPreviousEntry(Date d) throws SQLException {
        if (d == null) {
            return null;
        }
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_START_TIME + "< ? and " + DB.COL_STATUS + ">=0",
                new String[]{Long.toString(d.getTime() / 1000l)}, null, null, DB.COL_START_TIME + " DESC", "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return createTrackDescription(cursor);
                }
            } finally {
                DbUtil.close(cursor);
            }
        }
        return null;
    }

    public ActivityFactory getActivityFactory() {
        return activityFactory;
    }

    public Set<String> getAllTrackNames() {
        Cursor cursor = db().query(true, DB.DATABASE_TABLE, new String[]{DB.COL_NAME}, null, null, null, null, null, null);
        return createSet(cursor, 0);
    }

    public Set<String> getAllTrackPaths(boolean includeDeleted) {
        Cursor cursor = fetchAllEntriesAsCursor(includeDeleted);
        return createSet(cursor, DB.COL_MAPPING.get(DB.COL_PATH));
    }

    public boolean updateEntry(TrackDescriptionNG trackDescription) {
        String idString = Long.toString(trackDescription.getId());
        boolean success = db().update(DB.DATABASE_TABLE, toContentValues(trackDescription), DB.COL_ID + "=?", new String[]{idString}) > 0;
        if (success) {
            db().delete(DB_EXT.DATABASE_TABLE, DB_EXT.COL_TRACK_ID + "=?", new String[]{idString});
            List<ContentValues> ce = extrasToContentValues(trackDescription, trackDescription.getId());
            for (ContentValues cv : ce) {
                db().insert(DB_EXT.DATABASE_TABLE, null, cv);
            }
        }
        return success;
    }

    private String validatePath(String path) {
        // since some versions, apemap adds a # and some extra info to the path when re-reading it
        File pathFile = new File(path);
        if (pathFile.exists()) {
            return path;
        }
        File pathDir = pathFile.getParentFile();
        if (pathDir == null) {
            List<Property> trackFolders = Configuration.getInstance().getMultiValueDbProperty(Configuration.PROPERTY_TRACK_FOLDER);
            for (Property folder : trackFolders) {
                if (folder.getValue() != null) {
                    File f = new File(correctApemapModifiedPath(path, new File(folder.getValue())));
                    if (f.exists()) {
                        return f.getAbsolutePath();
                    }
                }
            }
            return path;
        }
        return correctApemapModifiedPath(path, pathDir);
    }

    public static class DB {

        public static final String COL_START_TIME = "startTime";
        public static final String COL_END_TIME = "endTime";
        public static final String COL_MOVING_TIME = "movingTime";
        public static final String COL_NAME = "name";
        public static final String COL_PATH = "path";
        public static final String COL_DIST_H = "distHor";
        public static final String COL_DIST_V_UP = "distVertUp";
        public static final String COL_AVG_PULSE = "avgPulse";
        public static final String COL_MAX_PULSE = "maxPulse";
        public static final String COL_COMMENT = "comment";
        public static final String COL_ID = "id";

        /**
         * 1 regular entry<br/>
         * 0 entry resulting from a copy operation -> does not get deleted by clear()<br/>
         * -1 deleted entry (used to exclude the entry from being re-loaded by "load new tracks)<br/>
         */
        public static final String COL_STATUS = "status";

        public static final String DATABASE_TABLE = "tracks";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_START_TIME
                + " integer not null, " + COL_END_TIME + " integer not null, " + COL_NAME + " text not null, " + COL_PATH + " text not null," + COL_MOVING_TIME
                + " integer not null, " + COL_DIST_H + " integer not null, " + COL_DIST_V_UP + " integer not null, " + COL_STATUS + " integer default 0, " + COL_AVG_PULSE + " integer default 0, " + COL_MAX_PULSE + " integer default 0);";

        public static final String DATABASE_CREATE_INDEX_DATE = "create index if not exists INDEX_TRACK_START on " + DATABASE_TABLE + " (" + COL_START_TIME
                + ")";
        public static final String DATABASE_CREATE_INDEX_PATH = "create index if not exists INDEX_TRACK_PATH on " + DATABASE_TABLE + " (" + COL_PATH + ")";

        public static final String[] COL_NAMES = new String[]{COL_ID, COL_START_TIME, COL_END_TIME, COL_NAME, COL_PATH, COL_MOVING_TIME, COL_DIST_H,
                COL_DIST_V_UP, COL_AVG_PULSE, COL_MAX_PULSE};
        public static final Map<String, Integer> COL_MAPPING = DbUtil.createColMapping(COL_NAMES);
    }

    public static class DB_EXT {

        public static final String COL_TRACK_ID = "trackId";
        public static final String COL_VALUE = "value";
        public static final String COL_NAME = "name";
        public static final String COL_MULTIVALUE = "multivalue";

        public static final String DATABASE_TABLE = "trackExtras";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_TRACK_ID + " integer not null, " + COL_NAME
                + " text not null, " + COL_VALUE + " text not null, " + COL_MULTIVALUE + " integer default 0);";

        public static final String DATABASE_CREATE_INDEX = "create index if not exists INDEX_TRACK_EXTRAS on " + DATABASE_TABLE + " (" + COL_TRACK_ID + ", "
                + COL_MULTIVALUE + ")";

        public static final String[] COL_NAMES = new String[]{COL_TRACK_ID, COL_NAME, COL_VALUE, COL_MULTIVALUE};
        public static final Map<String, Integer> COL_MAPPING = DbUtil.createColMapping(COL_NAMES);
    }

    public static class ExtrasIterator extends AbstractCursorIterator<NameValuePair> {

        public ExtrasIterator(Cursor cursor) {
            super(cursor);
        }

        @Override
        public NameValuePair createObject() {
            return new NameValuePair(cursor.getString(DB_EXT.COL_MAPPING.get(DB_EXT.COL_NAME)), cursor.getString(DB_EXT.COL_MAPPING.get(DB_EXT.COL_VALUE)));
        }
    }

    public static class TrackIterator extends AbstractCursorIterator<TrackDescriptionNG> {

        private final TrackDbAdapter dbAdapter;

        public TrackIterator(Cursor cursor, TrackDbAdapter dbAdapter) {
            super(cursor);
            this.dbAdapter = dbAdapter;
        }

        @Override
        public TrackDescriptionNG createObject() {
            return dbAdapter.createTrackDescription(cursor, false);
        }
    }
}
