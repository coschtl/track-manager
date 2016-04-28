package at.dcosta.android.fw.props;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PropertyDatabaseHelper extends SQLiteOpenHelper {

	public PropertyDatabaseHelper(Context context) {
		super(context, "PropertyDb", null, 1);
		try {
			getWritableDatabase();
		} catch (Exception e) {
			System.exit(0);
		}
	}

	public void createBaseTables(SQLiteDatabase db) {
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE);
		createIndices(db);
	}

	public void createIndices(SQLiteDatabase db) {
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE_INDEX_NAME);
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE_INDEX_TYPE_NAME);
	}

	public void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + PropertyDbAdapter.DB.DATABASE_TABLE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createBaseTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("---> rebuilding property database");
		db.execSQL("DROP TABLE IF EXISTS " + PropertyDbAdapter.DB.DATABASE_TABLE);
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE);
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE_INDEX_NAME);
		db.execSQL(PropertyDbAdapter.DB.DATABASE_CREATE_INDEX_TYPE_NAME);
	}

}
