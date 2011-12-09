package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Parst eine flickr-XML-Antwort mit Sets und erzeugt daraus eine Liste von Sets
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class SetsReader extends XmlReader {

	public class Set {
		public String id;
		public String title;
		public String description;
		/**
		 * Titel-Bild des Sets
		 */
		public String primaryPhotoId;

		public String toString() {
			return "#" + id + ": " + title;
		}
	}

	public ArrayList<Set> getSets(String xmlData) {

		ArrayList<Set> sets = new ArrayList<Set>();

		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
				.newInstance();
		domBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			NodeList setNodes = doc.getElementsByTagName("photoset");
			for (int i = 0; i < setNodes.getLength(); i++) {
				Node setNode = setNodes.item(i);
				Set set = new Set();
				set.id = setNode.getAttributes().getNamedItem("id")
						.getNodeValue();
				set.primaryPhotoId = setNode.getAttributes().getNamedItem("primary")
						.getNodeValue(); 
				NodeList setChilds = setNode.getChildNodes();
				for (int j = 0; j < setChilds.getLength(); j++) {
					Node setChild = setChilds.item(j);
					if (setChild.getNodeName().equals("title")) {
						set.title = getTextValue(setChild);
					}
					if (setChild.getNodeName().equals("description")) {
						set.description = getTextValue(setChild);
					}
				}
				sets.add(set);
			}
		} catch (SAXException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return sets;
	}
}
