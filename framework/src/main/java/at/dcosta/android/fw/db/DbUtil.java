package at.dcosta.android.fw.db;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

public class DbUtil {

	public static void close(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	public static Map<String, Integer> createColMapping(String[] colNames) {
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		int i = 0;
		for (String col : colNames) {
			mapping.put(col, i++);
		}
		return mapping;
	}
}
