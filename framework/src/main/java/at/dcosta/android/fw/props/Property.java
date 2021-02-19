package at.dcosta.android.fw.props;

import at.dcosta.android.fw.NameValuePair;

public class Property extends PropertyTemplate {

    private static final long serialVersionUID = 1L;
    private final long id;
    private String value;

    public Property(long id, PropertyTemplate template) {
        super(template.getName(), template.getType());
        this.id = id;
        setCategory(template.getCategory());
        setDisplayName(template.getDisplayName());
        setHelpText(template.getHelpText());
        setMultivalue(template.isMultivalue());
        setAccess(template.getAccess());
        setValue(template.getDefaultValue());
        setPosition(template.getPosition());
        setPossibleValues(template.getPossibleValues());
    }

    public Property(PropertyTemplate template) {
        this(-1, template);
    }

    public boolean getBooleanValue(boolean defaultValue) {
        if (getValue() == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(getValue());
    }

    public long getId() {
        return id;
    }

    public int getIntValue(int defaultValue) {
        if (getValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLongValue(long defaultValue) {
        if (getValue() == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(getValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getValue() {
        return value;
    }

    public Property setValue(String value) {
        if (isAllowedValue(value)) {
            this.value = value;
            return this;
        } else {
            throw new ConfigurationException("Illegal value '" + value + "'! Property '" + getName() + "' allows only the following values: "
                    + getPossibleValues());
        }
    }

    private boolean isAllowedValue(String value) {
        if (value == null) {
            return true;
        }
        if (getPossibleValues() == null) {
            return true;
        }
        for (NameValuePair nvp : getPossibleValues().getValues()) {
            if (value.equals(nvp.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append(" = ").append(value).toString();
    }

}
