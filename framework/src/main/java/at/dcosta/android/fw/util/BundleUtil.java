package at.dcosta.android.fw.util;

import java.util.logging.Logger;

import android.os.Bundle;

public final class BundleUtil {

	private static final Logger LOGGER = Logger.getLogger(BundleUtil.class.getName());

	public static <T> T get(String name, Bundle bundle) {
		return get(name, null, bundle);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String name, T defaultValue, Bundle bundle) {
		T value = (T) bundle.get(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public static boolean getBoolean(String name, boolean defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(o.toString());
	}

	public static double getDouble(String name, double defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(o.toString());
		} catch (NumberFormatException e) {
			LOGGER.warning("Can not get an double value from '" + o.toString() + "' -> returning defaultValue");
			return defaultValue;
		}
	}

	public static float getFloat(String name, float defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(o.toString());
		} catch (NumberFormatException e) {
			LOGGER.warning("Can not get an float value from '" + o.toString() + "' -> returning defaultValue");
			return defaultValue;
		}
	}

	public static int getInteger(String name, int defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(o.toString());
		} catch (NumberFormatException e) {
			LOGGER.warning("Can not get an int value from '" + o.toString() + "' -> returning defaultValue");
			return defaultValue;
		}
	}

	public static long getLong(String name, long defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(o.toString());
		} catch (NumberFormatException e) {
			LOGGER.warning("Can not get an long value from '" + o.toString() + "' -> returning defaultValue");
			return defaultValue;
		}
	}

	public static short getshort(String name, short defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		try {
			return Short.parseShort(o.toString());
		} catch (NumberFormatException e) {
			LOGGER.warning("Can not get an short value from '" + o.toString() + "' -> returning defaultValue");
			return defaultValue;
		}
	}

	public static String getString(String name, String defaultValue, Bundle bundle) {
		Object o = bundle.get(name);
		if (o == null) {
			return defaultValue;
		}
		return o.toString();
	}

}
