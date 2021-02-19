package at.dcosta.android.fw.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AbstractDbAdapter {

    private final SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;
    private boolean closed = true;

    public AbstractDbAdapter(SQLiteOpenHelper databaseHelper) {
        dbHelper = databaseHelper;
    }

    public void close() {
        dbHelper.close();
        closed = true;
    }

    protected SQLiteDatabase db() {
        openIfNecessary();
        return db;
    }

    public void forceUpgrade(int versionTo) {
        dbHelper.onUpgrade(db(), 0, versionTo);
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new instance of the database. If it cannot be created, throw an exception to signal the
     * failure
     *
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AbstractDbAdapter openIfNecessary() throws SQLException {
        if (isClosed() || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
            closed = false;
        }
        return this;
    }

}
