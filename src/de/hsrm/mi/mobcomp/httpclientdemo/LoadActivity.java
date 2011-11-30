package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Demonstriert, wie man mit dem {@link HttpClient} Daten laden kann.
 * 
 * Verwendet dazu die flickr-API.
 * @see http://www.flickr.com/services/api/
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class LoadActivity extends Activity {
	private String flickrAPIKey;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load);

		SharedPreferences prefs = PrefsActivity.getPreferences(this);
		String keyFlickrAPIKey = getResources().getString(R.string.key_api_key);
		flickrAPIKey = prefs.getString(keyFlickrAPIKey, null);

		if (flickrAPIKey == null || flickrAPIKey.length() <= 0) {
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			Toast.makeText(getApplicationContext(),
					R.string.please_configure_api_key, Toast.LENGTH_LONG)
					.show();
			finish();
		}

		Log.v(getClass().getCanonicalName(), "flickr API Key: " + flickrAPIKey);

		FlickrRestAPI flickr = new FlickrRestAPI(flickrAPIKey);
		
		// TODO: Internetpermission
		// TODO: Thread

		// URL um meine flickr-Alben zu laden
		Uri galleriesUri = flickr.getGalleries();

		// HTTP-Client erzeugen
		HttpClient client = new DefaultHttpClient();

		// Wir machen einen GET-Request
		HttpGet request = new HttpGet(galleriesUri.toString());
		// mit request.setHeader(header, value) können beliebige Header im
		// Request gesetzt werden

		// Anfrage abschicken
		try {
			HttpResponse response = client.execute(request);
			
			// Http-Status auslesen
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != 200) {
				throw new IOException("Invalid response from server: "
						+ status.toString());
			}

			// Antwort auslesen
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}
			String dataAsString = new String(content.toByteArray());
			Log.d(getClass().getCanonicalName(), dataAsString);
		} catch (ClientProtocolException e) {
			// Ein Problem in der HTTP-Kommunikation ist aufgetreten
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(getClass().getCanonicalName(), e.getMessage());
		} catch (IOException e) {
			// Irgend ein andere I/O-Fehler ist aufgetreten
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
	}
}