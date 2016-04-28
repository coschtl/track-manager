package at.dcosta.android.fw.gui;

import java.util.HashMap;
import java.util.Map;

import at.dcosta.android.fw.ExtraBased;

public class IconListIdHolder extends ExtraBased<IconListIdHolder> {

	private static final long serialVersionUID = 1L;
	public static final String KEY_ADDITIONAL_ICON = "additionalIcon";

	private int listLayoutId, listRowLayoutId, headerId, lineHeadId, lineBodyId, lineIconId;
	private final Map<String, Integer> extras;

	public IconListIdHolder() {
		extras = new HashMap<String, Integer>();
	}

	public int getExtra(String key, int defaultValue) {
		Integer integer = extras.get(key);
		if (integer == null) {
			return defaultValue;
		}
		return integer.intValue();
	}

	public int getHeaderId() {
		return headerId;
	}

	public int getLineBodyId() {
		return lineBodyId;
	}

	public int getLineHeadId() {
		return lineHeadId;
	}

	public int getLineIconId() {
		return lineIconId;
	}

	public int getListLayoutId() {
		return listLayoutId;
	}

	public int getListRowLayoutId() {
		return listRowLayoutId;
	}

	public IconListIdHolder setHeaderId(int headerId) {
		this.headerId = headerId;
		return this;
	}

	public IconListIdHolder setLineBodyId(int lineBodyId) {
		this.lineBodyId = lineBodyId;
		return this;
	}

	public IconListIdHolder setLineHeadId(int lineHeadId) {
		this.lineHeadId = lineHeadId;
		return this;
	}

	public IconListIdHolder setLineIconId(int lineIconId) {
		this.lineIconId = lineIconId;
		return this;
	}

	public IconListIdHolder setListLayoutId(int listLayoutId) {
		this.listLayoutId = listLayoutId;
		return this;
	}

	public IconListIdHolder setListRowLayoutId(int listRowLayoutId) {
		this.listRowLayoutId = listRowLayoutId;
		return this;
	}
}
