package at.dcosta.android.fw.props;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import at.dcosta.android.fw.dom.ElementIterator;

public class PropertyConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, PropertyTemplate> properties;
    private int aktPosition = -1;

    public PropertyConfiguration(InputStream is) {
        properties = new HashMap<String, PropertyTemplate>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            Element root = doc.getDocumentElement();
            ElementIterator it = new ElementIterator(root);
            while (it.hasNext()) {
                PropertyTemplate property = parse(it.next());
                properties.put(property.getName(), property);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException("Eror reading Configuration XML file!", e);
        }
    }

    private boolean getBooleanAttribute(Element elm, String name) {
        String value = elm.getAttribute(name);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public PropertyTemplate getByName(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        }
        throw new ConfigurationException("No Property template found for '" + name + "'. Check configuration xml file!");
    }

    private String getMandatoryAttribute(String name, Element elm) {
        if (elm.hasAttribute(name)) {
            String value = elm.getAttribute(name);
            if (value.length() > 0) {
                return value;
            }
        }
        throw new ConfigurationException("Error parsing XML configuration: Element '" + elm.getTagName() + "' must have an attribute named '" + name + "'!");
    }

    public Collection<PropertyTemplate> getProperties() {
        return properties.values();
    }

    private String getUppercaseAttribute(Element elm, String name) {
        String value = elm.getAttribute(name);
        if (value == null) {
            return null;
        }
        return value.toUpperCase(Locale.US);
    }

    private PropertyTemplate parse(Element elm) {
        PropertyTemplate pt = new PropertyTemplate(getMandatoryAttribute("name", elm), getMandatoryAttribute("type", elm));
        String posString = elm.getAttribute("position");
        int pos = 0;
        if (posString != null && !"".equals(posString)) {
            pos = Integer.valueOf(posString);
        }
        if (pos <= aktPosition++) {
            pos = aktPosition;
        }
        pt.setPosition(Integer.valueOf(pos));
        pt.setAccess(Access.valueOf(getUppercaseAttribute(elm, "access")));
        pt.setMultivalue(getBooleanAttribute(elm, "multivalue"));
        ElementIterator it = new ElementIterator(elm);
        while (it.hasNext()) {
            Element child = it.next();
            String childName = child.getNodeName();
            String value = child.getTextContent();
            if ("displayname".equals(childName)) {
                pt.setDisplayName(value);
            } else if ("help".equals(childName)) {
                pt.setHelpText(value);
            } else if ("category".equals(childName)) {
                pt.setCategory(value);
            } else if ("possibleValue".equals(childName)) {
                String name = child.hasAttribute("displayname") ? child.getAttribute("displayname") : value;
                pt.addPossibleValue(name, value);
            } else if ("defaultValue".equals(childName)) {
                pt.setDefaultValue(value);
            }
        }
        System.out.println(pt);
        return pt;
    }

}
