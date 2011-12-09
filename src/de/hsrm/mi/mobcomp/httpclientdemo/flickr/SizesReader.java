package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.util.Log;

/**
 * Parst eine flickr-XML-Antwort mit Photo-Größen und erzeugt daraus eine Liste von Sizes
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class SizesReader {
	public class Size {
		public String id;
		public String type;
		public int width;
		public int height;
		public Uri source;
		public Uri url;

		public String toString() {
			return "#" + id + ": " + type + " " + source;
		}
	}

	private String id;

	public SizesReader(String id) {
		this.id = id;
	}

	public ArrayList<Size> getSizes(String xmlData) {

		ArrayList<Size> sizes = new ArrayList<Size>();

		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
				.newInstance();
		domBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			NodeList setNodes = doc.getElementsByTagName("size");
			for (int i = 0; i < setNodes.getLength(); i++) {
				Node setNode = setNodes.item(i);
				NamedNodeMap attr = setNode.getAttributes();
				Size size = new Size();
				size.id = id;
				size.type = attr.getNamedItem("label").getNodeValue();
				size.width = Integer.parseInt(attr.getNamedItem("width")
						.getNodeValue());
				size.height = Integer.parseInt(attr.getNamedItem("height")
						.getNodeValue());
				size.source = Uri.parse(attr.getNamedItem("source")
						.getNodeValue());
				size.url = Uri.parse(attr.getNamedItem("url").getNodeValue());
				sizes.add(size);
			}
		} catch (SAXException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return sizes;
	}
}
