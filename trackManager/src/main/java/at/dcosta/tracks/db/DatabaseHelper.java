package at.dcosta.tracks.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "trackmanager";
	public static final int DATABASE_VERSION = 7;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		try {
			getWritableDatabase();
		} catch (Exception e) {
			System.exit(0);
		}
	}

	public void createBaseTables(SQLiteDatabase db) {
		db.execSQL(TrackDbAdapter.DB.DATABASE_CREATE);
		db.execSQL(TrackDbAdapter.DB_EXT.DATABASE_CREATE);
		createIndices(db);
	}

	private void createIndices(SQLiteDatabase db) {
		db.execSQL(TrackDbAdapter.DB.DATABASE_CREATE_INDEX_DATE);
		db.execSQL(TrackDbAdapter.DB.DATABASE_CREATE_INDEX_PATH);
		db.execSQL(TrackDbAdapter.DB_EXT.DATABASE_CREATE_INDEX);

	}

	private void createSavedSearchTable(SQLiteDatabase db) {
		db.execSQL(SavedSearchesDbAdapter.DB.DATABASE_CREATE);
		db.execSQL(SavedSearchesDbAdapter.DB.DATABASE_CREATE_INDEX_DATE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createBaseTables(db);
		createSavedSearchTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 3) {
			Log.w("TrackManager", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + TrackDbAdapter.DB.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TrackDbAdapter.DB_EXT.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PointDbAdapter.DB.DATABASE_TABLE);
			createBaseTables(db);
		}
		Log.w("TrackManager", "Upgrading database from version " + oldVersion + " to " + newVersion + ".");
		if (oldVersion < 6) {
			db.execSQL("DROP TABLE IF EXISTS " + SavedSearchesDbAdapter.DB.DATABASE_TABLE);
			createSavedSearchTable(db);
		}

	}
}
