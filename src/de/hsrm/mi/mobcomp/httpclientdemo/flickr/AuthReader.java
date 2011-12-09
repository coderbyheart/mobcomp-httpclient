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
public class AuthReader extends XmlReader {
	public class Auth {
		public String token;
		public String perms;
		public User user = new User();
	}

	public class User {
		public String nsid;
		public String username;
		public String fullname;
	}

	public Auth getAuth(String xmlData) {
		Auth auth = new Auth();
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
				.newInstance();
		domBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = domBuilderFactory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			NodeList authNodes = doc.getElementsByTagName("auth").item(0).getChildNodes();
			for (int i = 0; i < authNodes.getLength(); i++) {
				Node child = authNodes.item(i);
				if (child.getNodeName().equals("token")) {
					auth.token = getTextValue(child);
				}
				if (child.getNodeName().equals("perms")) {
					auth.perms = getTextValue(child);
				}
				if (child.getNodeName().equals("user")) {
					auth.user.nsid = child.getAttributes().getNamedItem("nsid")
							.getNodeValue();
					auth.user.username = child.getAttributes()
							.getNamedItem("username").getNodeValue();
					auth.user.fullname = child.getAttributes()
							.getNamedItem("fullname").getNodeValue();
				}
			}
		} catch (SAXException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
		return auth;
	}
}
