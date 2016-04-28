package at.dcosta.android.fw.props;

import java.io.Serializable;

public class PropertyTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Class<?> getTypeClass(String type) {
		Class<?> c = null;
		try {
			c = Class.forName(type);
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("Can not get Class for Property-Type: " + type);
		}
		return c;
	}

	private final Class<?> type;
	private int position;
	private final String name;
	private String category, displayName, helpText, defaultValue;
	private boolean multivalue;
	private Access access;
	private ValueCollection valueCollection;

	protected PropertyTemplate(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public PropertyTemplate(String name, String type) {
		this(name, getTypeClass(type));
	}

	public void addPossibleValue(String displayname, String possibleValue) {
		if (valueCollection == null) {
			valueCollection = new ValueCollection();
			if (type != ValueCollection.class) {
				throw new ConfigurationException("Invalid type '" + type.getName()
						+ "': possibleValue can only get set, if type == at.dcosta.android.fw.props.ValueCollection.class");
			}
		}
		valueCollection.addValue(displayname, possibleValue);
	}

	public Access getAccess() {
		return access;
	}

	public String getCategory() {
		return category;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDisplayName() {
		if (displayName == null || displayName.length() < 1) {
			return getName();
		}
		return displayName;
	}

	public String getHelpText() {
		return helpText;
	}

	public String getName() {
		return name;
	}

	public int getPosition() {
		return position;
	}

	public ValueCollection getPossibleValues() {
		return valueCollection;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	void setAccess(Access access) {
		this.access = access;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	void setPosition(int position) {
		this.position = position;
	}

	protected void setPossibleValues(ValueCollection valueCollection) {
		this.valueCollection = valueCollection;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name).append(" [").append(displayName).append(", ").append(type);
		if (multivalue) {
			b.append(", multivalue");
		}
		b.append(", ").append(access);
		if (category != null) {
			b.append(", category='").append(category).append("'");
		}
		if (position > 0) {
			b.append(", pos=").append(position);
		}
		if (helpText != null) {
			b.append(", '").append(helpText).append("'");
		}
		if (valueCollection != null) {
			b.append(", ").append(valueCollection.toString());
		}
		b.append("]");
		return b.toString();
	}

}
