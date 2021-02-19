package at.dcosta.android.fw.util;

import java.util.Map;
import java.util.logging.Logger;

public final class PropertyUtil {

    private static final Logger LOGGER = Logger.getLogger(PropertyUtil.class.getName());

    public static <T> T get(String name, Map<String, ?> props) {
        return get(name, null, props);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String name, T defaultValue, Map<String, ?> props) {
        T value = (T) props.get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static boolean getBoolean(String name, boolean defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
        if (o == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(o.toString());
    }

    public static double getDouble(String name, double defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
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

    public static float getFloat(String name, float defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
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

    public static int getInteger(String name, int defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
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

    public static long getLong(String name, long defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
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

    public static short getshort(String name, short defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
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

    public static String getString(String name, String defaultValue, Map<String, ?> props) {
        Object o = props.get(name);
        if (o == null) {
            return defaultValue;
        }
        return o.toString();
    }

}
