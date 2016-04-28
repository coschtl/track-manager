package at.dcosta.android.fw;

import java.io.Serializable;

public class NameValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name, value;

	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if (name.equals(value)) {
			return name;
		}
		return new StringBuilder(name).append(" (").append(value).append(")").toString();
	}
}
