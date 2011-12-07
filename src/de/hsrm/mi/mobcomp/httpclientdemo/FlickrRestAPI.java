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
	private static final String baseUri = "http://api.flickr.com/services/rest/";
	private String apiKey;

	public FlickrRestAPI(String apiKey) {
		this.apiKey = apiKey;
	}

	public Uri getGalleries(String nsid) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("user_id", nsid);
		return getUri("flickr.galleries.getList", params);
	}

	private Uri getUri(String method) {
		return getUri(method, null);

	}

	private Uri getUri(String method, HashMap<String, String> params) {
		Builder uriBuilder = Uri.parse(baseUri).buildUpon()
				.appendQueryParameter("api_key", apiKey)
				.appendQueryParameter("method", method);
		if (params != null) {
			for (String key : params.keySet()) {
				uriBuilder.appendQueryParameter(key, params.get(key));
			}
		}
		return uriBuilder.build();
	}

}
