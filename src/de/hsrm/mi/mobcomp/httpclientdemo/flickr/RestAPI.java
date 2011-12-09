package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.util.HashMap;

import android.net.Uri;
import android.net.Uri.Builder;

/**
 * Helfer-Klasse für die flickr-REST-API
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class RestAPI {
	/**
	 * JSON wird auf Android leider erst ab API Level 11 unterstützt
	 * 
	 * @since API Level 11
	 */
	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_REST = "rest";
	public static final String FORMAT_XMPRPC = "xmlrpc";
	public static final String FORMAT_PHP_SERIAL = "php_serial";
	public static final String FORMAT_SOAP = "soap";

	public static final String SIZE_SQUARE = "Square";
	public static final String SIZE_THUMB = "Thumbnail";
	public static final String SIZE_SMALL = "Small";
	public static final String SIZE_MEDIUM = "Medium";
	public static final String SIZE_MEDIUM640 = "Medium 640";
	public static final String SIZE_ORIGINAL = "Original";

	private static final String baseUri = "http://api.flickr.com/services/rest/";
	private String apiKey;
	private String format = FORMAT_REST;

	public RestAPI(String apiKey) {
		this.apiKey = apiKey;
	}

	public RestAPI(String apiKey, String format) {
		this.apiKey = apiKey;
		this.format = format;
	}

	/**
	 * Gibt die URL zurück, unter der man die flickr-Sets eines Users findet
	 * 
	 * @param nsid
	 *            User-ID
	 * @param page
	 *            Welche Seite zurückgegeben werden soll
	 * @param per_page
	 *            Anzahl der Sets pro Seite
	 */
	public Uri getPhotoSets(String nsid, Integer page, Integer per_page) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", nsid);
		if (page != null)
			params.put("page", page.toString());
		if (per_page != null)
			params.put("per_page", per_page.toString());
		return getUri("flickr.photosets.getList", params);
	}

	private Uri getUri(String method, HashMap<String, String> params) {
		Builder uriBuilder = Uri.parse(baseUri).buildUpon()
				.appendQueryParameter("api_key", apiKey)
				.appendQueryParameter("method", method)
				.appendQueryParameter("format", format);
		if (params != null) {
			for (String key : params.keySet()) {
				uriBuilder.appendQueryParameter(key, params.get(key));
			}
		}
		return uriBuilder.build();
	}

	/**
	 * Gibt die URL zurück, unter der man die Größen eines flickr-Photos findet
	 * 
	 * @param photoId
	 * @return
	 */
	public Uri getPhotoSizes(String photoId) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("photo_id", photoId);
		return getUri("flickr.photos.getSizes", params);
	}

}
