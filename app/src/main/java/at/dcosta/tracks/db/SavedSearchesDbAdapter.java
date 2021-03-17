package at.dcosta.tracks.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.db.AbstractCursorIterator;
import at.dcosta.android.fw.db.AbstractDbAdapter;
import at.dcosta.android.fw.db.DbUtil;
import at.dcosta.tracks.util.SavedSearch;

/**
 * Simple property database access helper class.
 * <p>
 * This has been improved from the first version of this tutorial through the addition of better error handling and also using returning a Cursor instead of
 * using a collection of inner classes (which is less scalable and not recommended).
 */
public class SavedSearchesDbAdapter extends AbstractDbAdapter {

	public SavedSearchesDbAdapter(SQLiteOpenHelper databaseHelper, Context context) {
		super(databaseHelper);
	}

	public void add(SavedSearch savedSearch) {
		deleteEntry(savedSearch.getAlias());
		ContentValues cv = new ContentValues();
		cv.put(DB.COL_ALIAS, savedSearch.getAlias());
		cv.put(DB.COL_NAME, savedSearch.getName());
		cv.put(DB.COL_ACTIVITY, savedSearch.getActivity());
		cv.put(DB.COL_DATE_FROM, DateUtil.getEpochSecs(savedSearch.getDateStart()));
		cv.put(DB.COL_DATE_TO, DateUtil.getEpochSecs(savedSearch.getDateEnd()));
		db().insert(DB.DATABASE_TABLE, null, cv);
	}

	public int clear() {
		return db().delete(DB.DATABASE_TABLE, null, null);
	}

	private SavedSearch createSavedSearch(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			return new SavedSearch(cursor.getString(DB.COL_MAPPING.get(DB.COL_ALIAS)), cursor.getString(DB.COL_MAPPING.get(DB.COL_NAME)),
					cursor.getString(DB.COL_MAPPING.get(DB.COL_ACTIVITY)), cursor.getLong(DB.COL_MAPPING.get(DB.COL_DATE_FROM)), cursor.getLong(DB.COL_MAPPING
					.get(DB.COL_DATE_TO)));
		}
		return null;
	}

	/**
	 * Delete the property with the given alias
	 *
	 * @param alias alias of entry to delete
	 */
	public boolean deleteEntry(String alias) {
		return db().delete(DB.DATABASE_TABLE, DB.COL_ALIAS + " =?", new String[]{alias}) > 0;
	}

	public List<String> fetchAllAliases() {
		List<String> l = new ArrayList<String>();
		Iterator<SavedSearch> it = fetchAllEntries();
		while (it.hasNext()) {
			SavedSearch next = it.next();
			l.add(next.getAlias());
		}
		return l;
	}

	public Iterator<SavedSearch> fetchAllEntries() {
		return new SavedSearchIterator(fetchAllEntriesAsCursor(), this);
	}

	/**
	 * Return a Cursor over the list of all properties in the database
	 *
	 * @return Cursor over all properties
	 */
	public Cursor fetchAllEntriesAsCursor() {
		return db().query(DB.DATABASE_TABLE, DB.COL_NAMES, null, null, null, null, null);
	}

	public SavedSearch findEntry(String alias) throws SQLException {
		if (alias == null) {
			return null;
		}
		Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_ALIAS + "=?", new String[]{alias}, null, null, null, "1");
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					return createSavedSearch(cursor);
				}
			} finally {
				DbUtil.close(cursor);
			}
		}
		return null;
	}

	public static class DB {

		public static final String COL_ALIAS = "alias";
		public static final String COL_NAME = "name";
		public static final String COL_ACTIVITY = "activity";
		public static final String COL_DATE_FROM = "dateFrom";
		public static final String COL_DATE_TO = "dateTo";

		public static final String DATABASE_TABLE = "savedSearches";
		public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ALIAS + " text not null, " + COL_NAME + " text, "
				+ COL_ACTIVITY + " text, " + COL_DATE_FROM + " integer , " + COL_DATE_TO + " integer );";

		public static final String DATABASE_CREATE_INDEX_DATE = "create index if not exists INDEX_ALIAS on " + DATABASE_TABLE + " (" + COL_ALIAS + ")";

		public static final String[] COL_NAMES = new String[]{COL_ALIAS, COL_NAME, COL_ACTIVITY, COL_DATE_FROM, COL_DATE_TO};
		public static final Map<String, Integer> COL_MAPPING = DbUtil.createColMapping(COL_NAMES);
	}

	public static class SavedSearchIterator extends AbstractCursorIterator<SavedSearch> {

		private final SavedSearchesDbAdapter dbAdapter;

		public SavedSearchIterator(Cursor cursor, SavedSearchesDbAdapter dbAdapter) {
			super(cursor);
			this.dbAdapter = dbAdapter;
		}

		@Override
		public SavedSearch createObject() {
			return dbAdapter.createSavedSearch(cursor);
		}
	}

}
