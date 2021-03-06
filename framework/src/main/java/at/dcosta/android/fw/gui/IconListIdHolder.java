package at.dcosta.android.fw.gui;

import java.util.HashMap;
import java.util.Map;

import at.dcosta.android.fw.ExtraBased;

public class IconListIdHolder extends ExtraBased<IconListIdHolder> {

	public static final String KEY_ADDITIONAL_ICON = "additionalIcon";
	private static final long serialVersionUID = 1L;
	private final Map<String, Integer> extras;
	private int listLayoutId, listRowLayoutId, headerId, lineHeadId, lineBodyId, lineIconId;

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

	public IconListIdHolder setHeaderId(int headerId) {
		this.headerId = headerId;
		return this;
	}

	public int getLineBodyId() {
		return lineBodyId;
	}

	public IconListIdHolder setLineBodyId(int lineBodyId) {
		this.lineBodyId = lineBodyId;
		return this;
	}

	public int getLineHeadId() {
		return lineHeadId;
	}

	public IconListIdHolder setLineHeadId(int lineHeadId) {
		this.lineHeadId = lineHeadId;
		return this;
	}

	public int getLineIconId() {
		return lineIconId;
	}

	public IconListIdHolder setLineIconId(int lineIconId) {
		this.lineIconId = lineIconId;
		return this;
	}

	public int getListLayoutId() {
		return listLayoutId;
	}

	public IconListIdHolder setListLayoutId(int listLayoutId) {
		this.listLayoutId = listLayoutId;
		return this;
	}

	public int getListRowLayoutId() {
		return listRowLayoutId;
	}

	public IconListIdHolder setListRowLayoutId(int listRowLayoutId) {
		this.listRowLayoutId = listRowLayoutId;
		return this;
	}
}
