package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Parst eine flickr-XML-Antwort mit einem Frob
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class FrobReader extends XmlReader {
	public String getFrob(String xmlData) {
		String frob = null;
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
				.newInstance();
		domBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			NodeList frobNodes = doc.getElementsByTagName("frob");
			Node frobNode = frobNodes.item(0);
			frob = getTextValue(frobNode);
		} catch (SAXException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return frob;
	}
}
