package at.dcosta.android.fw.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementIterator implements Iterator<Element> {

	final NodeList childs;
	int pos;
	boolean ready;

	public ElementIterator(Node n) {
		childs = n.getChildNodes();
	}

	private void findNextElement() {
		while (pos < childs.getLength()) {
			Node n = childs.item(pos);
			if (nodeIsValid(n)) {
				ready = true;
				return;
			}
			pos++;
		}
		ready = false;
	}

	@Override
	public boolean hasNext() {
		if (!ready) {
			findNextElement();
		}
		return ready;
	}

	@Override
	public Element next() {
		if (!ready) {
			findNextElement();
		}
		if (ready) {
			ready = false;
			return (Element) childs.item(pos++);
		}
		throw new NoSuchElementException();
	}

	boolean nodeIsValid(Node n) {
		return n.getNodeType() == Node.ELEMENT_NODE;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove ist not implemented!");
	}

}