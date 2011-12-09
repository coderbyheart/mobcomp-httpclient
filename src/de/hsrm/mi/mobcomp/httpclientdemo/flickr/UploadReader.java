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
 * Parst eine flickr-XML-Antwort eines Photo-Uploads
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class UploadReader extends XmlReader {
	public String getPhotoId(String xmlData) {
		String photoid = null;
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
				.newInstance();
		domBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			Log.v(getClass().getCanonicalName(), xmlData);
			DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			NodeList photoIdNodes = doc.getElementsByTagName("photoid");
			Node photoIdNode = photoIdNodes.item(0);
			photoid = getTextValue(photoIdNode);
		} catch (SAXException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return photoid;
	}
}
