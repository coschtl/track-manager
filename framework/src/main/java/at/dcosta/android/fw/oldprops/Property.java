package at.dcosta.android.fw.oldprops;

import at.dcosta.android.fw.gui.IconListBean;

public class Property extends IconListBean {

	private static final long serialVersionUID = 1L;
	private final int status;
	private final String type;

	public Property(long id, String type, String name, String value, int icon, int status) {
		super(id, value, name, icon);
		this.type = type;
		this.status = status;
	}

	public String getName() {
		return getBody();
	}

	public int getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return getHead();
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getId()).append("(").append(type).append("): ").append(getName()).append("=").append(getValue()).toString();
	}

}
