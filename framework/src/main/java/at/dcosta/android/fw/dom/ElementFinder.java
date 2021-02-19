package at.dcosta.android.fw.dom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementFinder {

    public static Element findFirstChild(Node n, String childName) {
        NamedElementIterator it = new NamedElementIterator(n, childName);
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

}
