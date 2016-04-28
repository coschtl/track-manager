package at.dcosta.tracks.util;

import android.content.Context;
import at.dcosta.tracks.R;

public class PropertyUtil {

	public static String getDescription(String propertyName, Context context) {
		if (propertyName.equals(Configuration.PROPERTY_PHOTO_FOLDER)) {
			return context.getString(R.string.property_descr_photo_path);
		}
		if (propertyName.equals(Configuration.PROPERTY_TRACK_FOLDER)) {
			return context.getString(R.string.property_descr_track_path);
		}
		return null;
	}

}
