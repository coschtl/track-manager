package at.dcosta.android.fw.props;

public enum Access {
	READ_ONLY(false, false), CHANGE_VALUE(true, false), FULL(true, true);

	private final boolean editable, fullaccess;

	private Access(boolean editable, boolean fullaccess) {
		this.editable = editable;
		this.fullaccess = fullaccess;
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isFullaccess() {
		return fullaccess;
	}

}
