package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.ExtraRunnable;
import de.hsrm.mi.mobcomp.httpclientdemo.xml.SetsReader;
import de.hsrm.mi.mobcomp.httpclientdemo.xml.SetsReader.Set;

/**
 * Demonstriert, wie man mit dem {@link HttpClient} Daten laden kann.
 * 
 * Verwendet dazu die flickr-API.
 * 
 * @see http://www.flickr.com/services/api/
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class LoadActivity extends Activity {
	private ProgressBar loadingProgress;
	private TextView loadingStatus;
	private Handler handler = new Handler();
	private String flickrAPIKey;
	private String flickrNSID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load);

		loadingProgress = (ProgressBar) findViewById(R.id.loadingProgressBar);
		loadingStatus = (TextView) findViewById(R.id.loadingStatus);

		loadSets();
	}

	/**
	 * @see http 
	 *      ://developer.android.com/guide/practices/design/responsiveness.html
	 */
	private void loadSets() {

		SharedPreferences prefs = PrefsActivity.getPreferences(this);
		String keyFlickrAPIKey = getResources().getString(R.string.key_api_key);
		String flickrNSIDKey = getResources().getString(R.string.key_nsid);
		flickrAPIKey = prefs.getString(keyFlickrAPIKey, null);
		flickrNSID = prefs.getString(flickrNSIDKey, null);

		if (flickrAPIKey == null || flickrAPIKey.length() <= 0) {
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			Toast.makeText(getApplicationContext(),
					R.string.please_configure_api_key, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				FlickrRestAPI flickr = new FlickrRestAPI(flickrAPIKey);

				// URL um meine flickr-Alben zu laden
				Uri setsUri = flickr.getPhotoSets(flickrNSID, 1, 10);
				handler.post(new ExtraRunnable<Uri>(setsUri) {
					@Override
					public void run() {
						loadingStatus
								.setText("Loading sets from from flickr ...");
					}
				});

				// HTTP-Client erzeugen
				HttpClient client = new DefaultHttpClient();

				// Wir machen einen GET-Request
				Log.v(getClass().getCanonicalName(), setsUri.toString());
				HttpGet request = new HttpGet(setsUri.toString());
				// mit request.setHeader(header, value) können beliebige Header
				// im
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
					handler.post(new ExtraRunnable<String>(dataAsString) {
						@Override
						public void run() {
							loadingStatus.setText("Sets loaded.");
							loadSetsFromResponse(getExtra());
						}
					});
				} catch (IOException e) {
					// Ein Problem in der HTTP-Kommunikation ist aufgetreten
					handler.post(new ExtraRunnable<String>(e.getMessage()) {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), getExtra(),
									Toast.LENGTH_LONG).show();
						}
					});
					Log.e(getClass().getCanonicalName(), e.getMessage());
				}
			}

		}).start();
	}

	private void loadSetsFromResponse(String dataAsString) {
		loadingStatus.setText("Parsing XML.");
		new Thread(new ExtraRunnable<String>(dataAsString) {
			@Override
			public void run() {
				handler.post(new ExtraRunnable<ArrayList<Set>>(new SetsReader()
						.getSets(getExtra())) {
					@Override
					public void run() {
						loadingStatus.setText("XML parsed. "
								+ getExtra().size() + " Sets loaded.");
						loadingProgress.setVisibility(View.INVISIBLE);
						setContentView(R.layout.sets);
						LayoutInflater li = getLayoutInflater();
						LinearLayout setsListView = (LinearLayout) findViewById(R.id.sets);
						for (Set set : getExtra()) {
							LinearLayout setItem = (LinearLayout) li.inflate(
									R.layout.sets_item, setsListView, false);
							TextView setItemText = (TextView) setItem
									.findViewById(R.id.steItemTextView);
							setItemText.setText(set.title);
							setsListView.addView(setItem);
							Log.v(getClass().getCanonicalName(), set.title);
						}

					}

				});
			}
		}).start();
	}
}