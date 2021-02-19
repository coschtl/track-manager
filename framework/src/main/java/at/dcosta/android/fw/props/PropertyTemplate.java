package at.dcosta.android.fw.props;

import java.io.Serializable;

public class PropertyTemplate implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Class<?> type;
    private final String name;
    private int position;
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

    private static final Class<?> getTypeClass(String type) {
        Class<?> c = null;
        try {
            c = Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Can not get Class for Property-Type: " + type);
        }
        return c;
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

    void setAccess(Access access) {
        this.access = access;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {
        if (displayName == null || displayName.length() < 1) {
            return getName();
        }
        return displayName;
    }

    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHelpText() {
        return helpText;
    }

    void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public ValueCollection getPossibleValues() {
        return valueCollection;
    }

    protected void setPossibleValues(ValueCollection valueCollection) {
        this.valueCollection = valueCollection;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isMultivalue() {
        return multivalue;
    }

    void setMultivalue(boolean multivalue) {
        this.multivalue = multivalue;
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
