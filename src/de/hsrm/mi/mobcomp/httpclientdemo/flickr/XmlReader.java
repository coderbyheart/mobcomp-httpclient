package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Hilfsklasse für die XML-Verarbeitung
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public abstract class XmlReader {

	/**
	 * Gibt den Text aus einem Element zurück
	 * 
	 * @param node
	 * @return
	 */
	protected String getTextValue(Node node) {
		if (node == null) return "";
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() <= 0) return "";
		return childNodes.item(0).getNodeValue();
	}

}
