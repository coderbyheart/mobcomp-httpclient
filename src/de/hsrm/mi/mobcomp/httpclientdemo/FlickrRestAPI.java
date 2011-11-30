package de.hsrm.mi.mobcomp.httpclientdemo;

import android.net.Uri;

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

	public Uri getGalleries() {
		return getUri("flickr.galleries.getList");
	}
	
	private Uri getUri(String method)
	{
		return Uri.parse(baseUri).buildUpon()
				.appendQueryParameter("api_key", apiKey)
				.appendQueryParameter("method", method)
				.build();

	}

}
