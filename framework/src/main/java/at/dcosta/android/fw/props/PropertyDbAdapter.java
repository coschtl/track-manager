package at.dcosta.android.fw.props;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import java.util.Iterator;
import java.util.Map;

import at.dcosta.android.fw.db.AbstractCursorIterator;
import at.dcosta.android.fw.db.AbstractDbAdapter;
import at.dcosta.android.fw.db.DbUtil;

public class PropertyDbAdapter extends AbstractDbAdapter {

	private final PropertyConfiguration propertyConfiguration;

	public PropertyDbAdapter(Context context, PropertyConfiguration propertyConfiguration) {
		super(new PropertyDatabaseHelper(context));
		this.propertyConfiguration = propertyConfiguration;
	}

	public void assurePropertiesInDb() {
		for (PropertyTemplate template : propertyConfiguration.getProperties()) {
			if (getProperty(template.getName()) == null) {
				createPropertyEntry(new Property(template));
			}
		}
	}

	public int clear() {
		return db().delete(DB.DATABASE_TABLE, null, null);
	}

	private Property createProperty(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		String name = cursor.getString(DB.COL_MAPPING.get(DB.COL_NAME));
		Property property = new Property(cursor.getLong(DB.COL_MAPPING.get(DB.COL_ID)), propertyConfiguration.getByName(name));
		property.setCategory(cursor.getString(DB.COL_MAPPING.get(DB.COL_CATEGORY)));
		property.setValue(cursor.getString(DB.COL_MAPPING.get(DB.COL_VALUE)));
		property.setPosition(cursor.getInt(DB.COL_MAPPING.get(DB.COL_POSITION)));
		return property;
	}

	/**
	 * Create a new property using the type, name and value provided. If the property is successfully created return the new rowId for that property, otherwise
	 * return a -1 to indicate failure.
	 *
	 * @return rowId or -1 if failed
	 */
	public long createPropertyEntry(Property property) {
		return db().insert(DB.DATABASE_TABLE, null, toContentValues(property));
	}

	/**
	 * Delete the property with the given rowId
	 *
	 * @param rowId id of property to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteProperty(long rowId) {
		return db().delete(DB.DATABASE_TABLE, DB.COL_ID + "=" + rowId, null) > 0;
	}

	public boolean deleteProperty(String name) {
		return db().delete(DB.DATABASE_TABLE, DB.COL_NAME + "=?", new String[]{name}) > 0;
	}

	public Iterator<Property> fetchAllProperties() {
		return fetchAllProperties(null);
	}

	public Iterator<Property> fetchAllProperties(String name) {
		Cursor cursor;
		if (name == null) {
			cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, null, null, null, null, DB.COL_POSITION + ", " + DB.COL_NAME);
		} else {
			cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_NAME + "=?", new String[]{name}, null, null, DB.COL_POSITION + ", " + DB.COL_NAME);
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
	 * @param rowId id of property to retrieve
	 * @return Cursor positioned to matching property, if found
	 * @throws SQLException if property could not be found/retrieved
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
		Cursor cursor = db().query(DB.DATABASE_TABLE, DB.COL_NAMES, DB.COL_NAME + "=?", new String[]{name}, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			DbUtil.close(cursor);
			return null;
		}
		Property prop = createProperty(cursor);
		DbUtil.close(cursor);
		return prop;
	}

	private ContentValues toContentValues(Property property) {
		ContentValues cv = new ContentValues();
		cv.put(DB.COL_CATEGORY, property.getCategory());
		cv.put(DB.COL_NAME, property.getName());
		cv.put(DB.COL_TYPE, property.getType().getName());
		cv.put(DB.COL_VALUE, property.getValue());
		cv.put(DB.COL_POSITION, property.getPosition());
		return cv;
	}

	/**
	 * Update the property using the details provided. The property to be updated is specified using the Id, and it is altered to use the type and value passed
	 * in
	 *
	 * @return true if the property was successfully updated, false otherwise
	 */
	public boolean updateProperty(Property property) {
		return db().update(DB.DATABASE_TABLE, toContentValues(property), DB.COL_ID + "=" + property.getId(), null) > 0;
	}

	public static class DB {

		public static final String DATABASE_TABLE = "properties";

		public static final String COL_ID = "id";
		public static final String COL_TYPE = "type";
		public static final String COL_NAME = "name";
		public static final String COL_VALUE = "value";
		public static final String COL_POSITION = "position";
		public static final String COL_CATEGORY = "category";
		public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_TYPE
				+ " text not null, " + COL_NAME + " text not null, " + COL_VALUE + " text, " + COL_POSITION + " integer, " + COL_CATEGORY + " text)";

		public static final String DATABASE_CREATE_INDEX_TYPE_NAME = "create index if not exists INDEX_TYPE_NAME on " + DATABASE_TABLE + " (" + COL_TYPE + ", "
				+ COL_NAME + ")";
		public static final String DATABASE_CREATE_INDEX_NAME = "create index if not exists INDEX_NAME on " + DATABASE_TABLE + " (" + COL_NAME + ")";
		public static final String[] COL_NAMES = new String[]{COL_ID, COL_TYPE, COL_NAME, COL_VALUE, COL_POSITION, COL_CATEGORY};
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
}
