package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class XmlReader {

	protected String getTextValue(Node node) {
		if (node == null) return "";
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() <= 0) return "";
		return childNodes.item(0).getNodeValue();
	}

}
