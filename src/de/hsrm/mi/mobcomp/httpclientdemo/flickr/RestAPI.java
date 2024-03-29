package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.TreeMap;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

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

	public static final String PERM_READ = "read";
	public static final String PERM_WRITE = "write";
	public static final String PERM_DELETE = "delete";

	private static final Uri baseUri = Uri
			.parse("http://api.flickr.com/services/rest/");
	private String apiKey;
	private String apiSecret;
	private String format = FORMAT_REST;
	private String authToken;

	public RestAPI(String apiKey) {
		this.setApiKey(apiKey);
	}

	public RestAPI(String apiKey, String apiSecret) {
		this.setApiKey(apiKey);
		this.setApiSecret(apiSecret);
	}

	public RestAPI(String apiKey, String apiSecret, String authToken) {
		this.setApiKey(apiKey);
		this.setApiSecret(apiSecret);
		this.setAuthToken(authToken);
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
		Builder uriBuilder = baseUri.buildUpon()
				.appendQueryParameter("api_key", getApiKey())
				.appendQueryParameter("method", method)
				.appendQueryParameter("format", format);
		if (params != null) {
			for (String key : params.keySet()) {
				uriBuilder.appendQueryParameter(key, params.get(key));
			}
		}
		return uriBuilder.build();
	}

	private Uri getSignedUri(String method) {
		return getSignedUri(method, null);
	}

	private Uri getSignedUri(String method, TreeMap<String, String> params) {
		return getSignedUri(method, params, baseUri);
	}

	private Uri getSignedUri(String method, TreeMap<String, String> params,
			Uri uri) {
		params.put("api_key", getApiKey());
		params.put("method", method);
		params.put("format", format);
		if (getAuthToken() != null)
			params.put("auth_token", getAuthToken());
		Builder uriBuilder = uri.buildUpon();
		for (String key : params.keySet()) {
			uriBuilder.appendQueryParameter(key, params.get(key));
		}
		uriBuilder.appendQueryParameter("api_sig", sign(params));
		return uriBuilder.build();
	}
	
	/**
	 * Einige Requests an die API müssen signiert werden.
	 * 
	 * @param params
	 * @return hash der Signatur
	 */
	public String sign(TreeMap<String, String> params)
	{
		String sig = getApiSecret();
		for (String key : params.keySet()) {
			sig += key + params.get(key);
		}
		return md5(sig);
	}

	private String md5(String text) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(getClass().getCanonicalName(), e.toString());
			return null;
		}
		m.reset();
		m.update(text.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
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

	public Uri getAuthFrobUri() {
		return this.getSignedUri("flickr.auth.getFrob");
	}

	public Uri getAuthUri(String frob, String perms) {
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("frob", frob);
		params.put("perms", perms);
		return getSignedUri("flickr.auth.getFrob", params,
				Uri.parse("http://flickr.com/services/auth/"));
	}

	public Uri getFullTokenUri(String token) {
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("mini_token", token);
		return getSignedUri("flickr.auth.getFullToken", params);
	}

	public Uri getUploadUri() {
		return Uri.parse("http://api.flickr.com/services/upload/");
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
}
