package de.hsrm.mi.mobcomp.httpclientdemo;

import java.util.HashMap;

import android.net.Uri;
import android.net.Uri.Builder;

/**
 * Helfer-Klasse für die flickr-REST-API
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class FlickrRestAPI {
	/**
	 * @since API Level 11
	 */
	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_REST = "rest";
	public static final String FORMAT_XMPRPC = "xmlrpc";
	public static final String FORMAT_PHP_SERIAL = "php_serial";
	public static final String FORMAT_SOAP = "soap";
	private static final String baseUri = "http://api.flickr.com/services/rest/";
	private String apiKey;
	private String format = FORMAT_REST;

	public FlickrRestAPI(String apiKey) {
		this.apiKey = apiKey;
	}

	public FlickrRestAPI(String apiKey, String format) {
		this.apiKey = apiKey;
		this.format = format;
	}

	public Uri getGalleries(String nsid) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", nsid);
		return getUri("flickr.galleries.getList", params);
	}
	
	public Uri getPhotoSets(String nsid, Integer page, Integer per_page) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", nsid);
		if (page != null) params.put("page", page.toString());
		if (per_page != null) params.put("per_page", per_page.toString());
		return getUri("flickr.photosets.getList", params);
	}

	private Uri getUri(String method) {
		return getUri(method, null);
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

}
