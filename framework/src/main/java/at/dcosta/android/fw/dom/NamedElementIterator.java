package at.dcosta.android.fw.dom;

import org.w3c.dom.Node;

public class NamedElementIterator extends ElementIterator {

	private final String nodeName;

	public NamedElementIterator(Node n, String nodeName) {
		super(n);
		if (nodeName == null) {
			throw new IllegalArgumentException("nodeName must not be null!");
		}
		this.nodeName = nodeName;
	}

	@Override
	boolean nodeIsValid(Node n) {
		return n.getNodeType() == Node.ELEMENT_NODE && nodeName.equals(n.getNodeName());
	}

}
