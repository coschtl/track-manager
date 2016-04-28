package at.dcosta.android.fw;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExtraBased<E> implements Serializable {

	private static final long serialVersionUID = 1L;
	protected final Map<String, Object> extras;

	public ExtraBased() {
		extras = new HashMap<String, Object>();
	}

	public boolean getBooleanExtra(String name, boolean defaultValue) {
		Object extra = getExtra(name);
		if (extra == null) {
			return defaultValue;
		}
		if (extra instanceof Boolean) {
			return ((Boolean) extra).booleanValue();
		}
		return Boolean.parseBoolean(extra.toString());
	}

	public Object getExtra(String name) {
		return extras.get(name);
	}

	public int getIntExtra(String name, int defaultValue) {
		Object extra = getExtra(name);
		if (extra == null) {
			return defaultValue;
		}
		if (extra instanceof Integer) {
			return ((Integer) extra).intValue();
		}
		try {
			return Integer.parseInt(extra.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public String getStringExtra(String name) {
		return getExtra(name).toString();
	}

	@SuppressWarnings("unchecked")
	public E setExtra(String key, Object value) {
		extras.put(key, value);
		return (E) this;
	}

}
