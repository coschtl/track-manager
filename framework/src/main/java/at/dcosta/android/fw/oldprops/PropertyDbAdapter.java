package at.dcosta.android.fw.oldprops;

import java.util.Iterator;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import at.dcosta.android.fw.db.AbstractCursorIterator;
import at.dcosta.android.fw.db.AbstractDbAdapter;
import at.dcosta.android.fw.db.DbUtil;

/**
 * Simple property database access helper class.
 * 
 * This has been improved from the first version of this tutorial through the addition of better error handling and also using returning a Cursor instead of
 * using a collection of inner classes (which is less scalable and not recommended).
 */
public class PropertyDbAdapter extends AbstractDbAdapter {

	public static class DB {

		public static final String DATABASE_TABLE = "properties";

		public static final String COL_TYPE = "type";
		public static final String COL_NAME = "name";
		public static final String COL_VALUE = "value";
		public static final String COL_ID = "id";
		public static final String COL_STATUS = "status";
		public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_TYPE
				+ " text not null, " + COL_NAME + " text not null, " + COL_VALUE + " text not null, " + COL_STATUS + " integer);";

		public static final String DATABASE_CREATE_INDEX_TYPE_NAME = "create index if not exists INDEX_TYPE_NAME on " + DATABASE_TABLE + " (" + COL_TYPE + ", "
				+ COL_NAME + ")";
		public static final String DATABASE_CREATE_INDEX_NAME = "create index if not exists INDEX_NAME on " + DATABASE_TABLE + " (" + COL_NAME + ")";
		public static final String[] COL_NAMES = new String[] { COL_ID, COL_TYPE, COL_NAME, COL_VALUE, COL_STATUS };
		public static final Map<String, Integer> COL_MAPPING = DbUtil.createColMapping(COL_NAMES);
	}

	public class PropertyIterator extends AbstractCursorIterator<Property> {

		public PropertyIterator(Cursor cursor) {
			super(cursor);
		}

		@Override
		public Property createObject() {
			return createProperty(cursor);
		}

	}

	private final PropertyIds ids;

	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public PropertyDbAdapter(SQLiteOpenHelper databaseHelper, PropertyIds ids) {
		super(databaseHelper);
		this.ids = ids;
	}

	private Property createProperty(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		String type = cursor.getString(DB.COL_MAPPING.get(DB.COL_TYPE));
		return new Property(cursor.getLong(DB.COL_MAPPING.get(DB.COL_ID)), type, cursor.getString(DB.COL_MAPPING.get(DB.COL_NAME)),
				cursor.getString(DB.COL_MAPPING.get(DB.COL_VALUE)), ids.getIcon(type), cursor.getInt(DB.COL_MAPPING.get(DB.COL_STATUS)));
	}

	/**
	 * Create a new property using the type, name and value provided. If the property is successfully created return the new rowId for that property, otherwise
	 * return a -1 to indicate failure.
	 * 
	 * @param type
	 *            the type of the property
	 * @param name
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 * @return rowId or -1 if failed
	 */
	public long createPropertyEntry(String type, String name, String value, int status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DB.COL_TYPE, type);
		initialValues.put(DB.COL_NAME, name);
		initialValues.put(DB.COL_VALUE, value);
		initialValues.put(DB.COL_STATUS, status);

		return db().insert(DB.DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the property with the given rowId
	 * 
	 * @param rowId
	 *            id of property to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteProperty(long rowId) {
		return db().delete(DB.DATABASE_TABLE, DB.COL_ID + "=" + rowId, null) > 0;
	}

	public boolean deleteProperty(String type, String name) {
		return db().delete(DB.DATABASE_TABLE, DB.COL_TYPE + "=?" + " and " + DB.COL_NAME + "=?", new String[] { type, name }) > 0;
	}

	public Iterator<Property> fetchAllProperties() {
		return fetchAllProperties(null);
	}

	public Iterator<Property> fetchAllProperties(String name) {
		Cursor cursor;
		if (name == null) {
			cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, null, null, null, null, DB.COL_VALUE);
		} else {
			cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_NAME + "=?", new String[] { name }, null, null, DB.COL_VALUE);
		}
		return new PropertyIterator(cursor);
	}

	public Property fetchProperty(long rowId) throws SQLException {
		Cursor cursor = fetchPropertyCursor(rowId);
		Property prop = createProperty(cursor);
		DbUtil.close(cursor);
		return prop;
	}

	/**
	 * Return a Cursor positioned at the property that matches the given rowId
	 * 
	 * @param rowId
	 *            id of property to retrieve
	 * @return Cursor positioned to matching property, if found
	 * @throws SQLException
	 *             if property could not be found/retrieved
	 */
	public Cursor fetchPropertyCursor(long rowId) throws SQLException {
		Cursor cursor = db().query(true, DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_ID + "=" + rowId, null, null, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			DbUtil.close(cursor);
			return null;
		}
		return cursor;
	}

	/**
	 * Return a singleValue property, or the first value, if the property is a multiValue property, return the first one
	 * 
	 * @return The property or null
	 */
	public Property getProperty(String name) {
		Cursor cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_NAME + "=?", new String[] { name }, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			DbUtil.close(cursor);
			return null;
		}
		Property prop = createProperty(cursor);
		DbUtil.close(cursor);
		return prop;
	}

	public long setPropertyEntry(String type, String name, String value, int status) {
		deleteProperty(type, name);
		return createPropertyEntry(type, name, value, status);
	}

	/**
	 * Update the property using the details provided. The property to be updated is specified using the Id, and it is altered to use the type and value passed
	 * in
	 * 
	 * @param id
	 *            id of property to update
	 * @param type
	 *            value to set property type to
	 * @param value
	 *            value to set property value to
	 * @return true if the property was successfully updated, false otherwise
	 */
	public boolean updateProperty(long id, String type, String name, String value, int status) {
		ContentValues args = new ContentValues();
		args.put(DB.COL_TYPE, type);
		args.put(DB.COL_NAME, name);
		args.put(DB.COL_VALUE, value);
		args.put(DB.COL_STATUS, status);

		return db().update(DB.DATABASE_TABLE, args, DB.COL_ID + "=" + id, null) > 0;
	}
}
