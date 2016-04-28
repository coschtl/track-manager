package at.dcosta.android.fw;

public class IdValuePair {
	private int id;
	private String value;

	public IdValuePair(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
