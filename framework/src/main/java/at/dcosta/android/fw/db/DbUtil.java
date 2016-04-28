package at.dcosta.android.fw.db;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

public class DbUtil {

	public static final void close(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	public static final Map<String, Integer> createColMapping(String[] colNames) {
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		int i = 0;
		for (String col : colNames) {
			mapping.put(col, i++);
		}
		return mapping;
	}
}
