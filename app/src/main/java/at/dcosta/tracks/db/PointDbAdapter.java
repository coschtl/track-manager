package at.dcosta.tracks.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.db.AbstractCursorIterator;
import at.dcosta.android.fw.db.AbstractDbAdapter;
import at.dcosta.tracks.track.Point;

public class PointDbAdapter extends AbstractDbAdapter {

    public PointDbAdapter(Context context) {
        super(new DatabaseHelper(context));
    }

    private static ContentValues toContentValues(Point point) {
        ContentValues cv = new ContentValues();
        if (point.getId() > 0) {
            cv.put(DB.COL_ID, point.getId());
        }
        cv.put(DB.COL_TRK_ID, point.getTrackId());
        cv.put(DB.COL_LAT, point.getLat());
        cv.put(DB.COL_LON, point.getLon());
        cv.put(DB.COL_TIME, DateUtil.getEpochSecs(point.getTimeStampAsDate()));
        cv.put(DB.COL_HEIGHT, point.getHeight());
        return cv;
    }

    public int clear() {
        return db().delete(DB.DATABASE_TABLE, null, null);
    }

    public void createEntries(long trackId, List<Point> points) {
        SQLiteDatabase db = db();
        db.beginTransaction();
        SQLiteStatement st = db.compileStatement(DB.INSERT);
        for (Point point : points) {
            st.bindLong(1, trackId);
            st.bindLong(2, DateUtil.getEpochSecs(point.getTimeStampAsDate()));
            st.bindDouble(3, point.getLat());
            st.bindDouble(4, point.getLon());
            st.bindLong(5, point.getHeight());
            st.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public long createEntry(Point point) {
        return db().insert(DB.DATABASE_TABLE, null, toContentValues(point));
    }

    private Point createPoint(Cursor cursor) {
        return createPoint(cursor, true);
    }

    private Point createPoint(Cursor cursor, boolean closeCursor) {
        if (cursor != null && !cursor.isClosed()) {
            Point p = new Point(cursor.getDouble(DB.COL_MAPPING.get(DB.COL_LAT)), cursor.getDouble(DB.COL_MAPPING.get(DB.COL_LON)),
                    cursor.getInt(DB.COL_MAPPING.get(DB.COL_HEIGHT)), 1000L * cursor.getLong(DB.COL_MAPPING.get(DB.COL_TIME)));
            p.setId(cursor.getLong(DB.COL_MAPPING.get(DB.COL_ID)));
            p.setId(cursor.getLong(DB.COL_MAPPING.get(DB.COL_TRK_ID)));
            if (closeCursor) {
                cursor.close();
            }
            return p;
        }
        return null;
    }

    public boolean deleteEntries(long trackId) {
        return db().delete(DB.DATABASE_TABLE, DB.COL_TRK_ID + "=?", new String[]{Long.toString(trackId)}) > 0;
    }

    public Iterator<Point> fetchAllEntries(long trackId) {
        return new PointIterator(fetchAllEntriesAsCursor(trackId), this);
    }

    public Cursor fetchAllEntriesAsCursor(long trackId) {
        return db().query(DB.DATABASE_TABLE, DB.COl_NAMES, DB.COL_TRK_ID + "=?", new String[]{Long.toString(trackId)}, null, null, DB.COL_ID);
    }

    public Point fetchEntry(long rowId) throws SQLException {
        return createPoint(fetchEntryAsCursor(rowId));
    }

    public Cursor fetchEntryAsCursor(long rowId) throws SQLException {

        Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COl_NAMES, DB.COL_ID + "=?", new String[]{Long.toString(rowId)}, null, null, DB.COL_ID, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor;
            }
        }
        return null;
    }

    public boolean updateEntry(Point point) {
        String idString = Long.toString(point.getId());
        return db().update(DB.DATABASE_TABLE, toContentValues(point), DB.COL_ID + "=?", new String[]{idString}) > 0;
    }

    public static class DB {

        public static final String COL_LAT = "lat";
        public static final String COL_LON = "lon";
        public static final String COL_TIME = "time";
        public static final String COL_HEIGHT = "height";
        public static final String COL_TRK_ID = "trkId";
        public static final String COL_ID = "id";

        public static final String DATABASE_TABLE = "trackpoints";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_TRK_ID
                + " integer not null, " + COL_TIME + " integer not null, " + COL_LAT + " double not null, " + COL_LON + " double not null," + COL_HEIGHT
                + " integer not null);";

        public static final String INSERT = "insert into " + DATABASE_TABLE + " (" + COL_TRK_ID + ", " + COL_TIME + ", " + COL_LAT + ", " + COL_LON + ", "
                + COL_HEIGHT + ") values (?,?,?,?,?)";
        public static final String DATABASE_CREATE_INDEX = "create index if not exists INDEX_POINTS_TRACK_ID on " + DATABASE_TABLE + " (" + COL_TRK_ID + ")";
        public static final String[] COl_NAMES = new String[]{COL_ID, COL_TRK_ID, COL_LAT, COL_LON, COL_TIME, COL_HEIGHT};
        private static final Map<String, Integer> COL_MAPPING;

        static {
            COL_MAPPING = new HashMap<String, Integer>();
            int i = 0;
            for (String col : COl_NAMES) {
                COL_MAPPING.put(col, i++);
            }
        }
    }

    public static class PointIterator extends AbstractCursorIterator<Point> {

        private final PointDbAdapter dbAdapter;

        public PointIterator(Cursor cursor, PointDbAdapter dbAdapter) {
            super(cursor);
            this.dbAdapter = dbAdapter;
        }

        @Override
        public Point createObject() {
            return dbAdapter.createPoint(cursor, false);
        }
    }
}
