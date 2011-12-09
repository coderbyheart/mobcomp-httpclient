package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.AuthReader;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.RestAPI;

/**
 * Autorisiert diese App bei flickr
 * 
 * @see http://www.flickr.com/services/api/
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class FlickrAuthActivity extends MenuActivity {

	private String flickrAPIKey;
	private RestAPI flickrAPI;
	private String flickrAPISecret;
	private String keyFlickrAPIAuthToken;
	private Handler handler = new Handler();
	private EditText token1;
	private EditText token2;
	private EditText token3;
	private Button doneButton;
	private String miniToken;
	private SharedPreferences prefs;
	private String flickrAPIAuthToken;
	private Button loginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PrefsActivity.getPreferences(this);
		String keyFlickrAPIKey = getResources().getString(R.string.key_api_key);
		String keyFlickrAPISecret = getResources().getString(
				R.string.key_api_secret);
		keyFlickrAPIAuthToken = getResources().getString(
				R.string.key_api_auth_token);
		flickrAPIKey = prefs.getString(keyFlickrAPIKey, null);
		flickrAPISecret = prefs.getString(keyFlickrAPISecret, null);
		flickrAPIAuthToken = prefs.getString(keyFlickrAPIAuthToken, null);

		// Keine Einstellung vorhanden? Dann Konfigurieren.
		if (flickrAPIKey == null || flickrAPIKey.length() <= 0
				|| flickrAPISecret == null || flickrAPISecret.length() <= 0) {
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			Toast.makeText(getApplicationContext(),
					R.string.please_configure_api_key, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		if (flickrAPIAuthToken != null && flickrAPIAuthToken.length() > 0) {
			startActivity(new Intent(getApplicationContext(),
					SendActivity.class));
			finish();
			return;
		}

		setContentView(R.layout.auth);

		loginButton = (Button) findViewById(R.id.loginButton);
		doneButton = (Button) findViewById(R.id.doneButton);
		token1 = (EditText) findViewById(R.id.token1);
		token2 = (EditText) findViewById(R.id.token2);
		token3 = (EditText) findViewById(R.id.token3);

		flickrAPI = new RestAPI(flickrAPIKey, flickrAPISecret);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// getFrob();
				token1.setEnabled(true);
				token2.setEnabled(true);
				token3.setEnabled(true);
				findViewById(R.id.enterTokenLayout).setVisibility(View.VISIBLE);
				doneButton.setVisibility(View.VISIBLE);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.flickr.com/auth-72157628220111349")));

			}
		});

		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				miniToken = token1.getText().toString() + "-"
						+ token2.getText().toString() + "-"
						+ token3.getText().toString();
				getFullToken();
			}
		});
	}

	private void getFullToken() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(flickrAPI.getFullTokenUri(
						miniToken).toString());
				try {
					HttpResponse response = client.execute(request);

					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() != 200) {
						throw new IOException("Invalid response from server: "
								+ status.toString());
					}

					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();
					ByteArrayOutputStream content = new ByteArrayOutputStream();
					int readBytes = 0;
					byte[] sBuffer = new byte[512];
					while ((readBytes = inputStream.read(sBuffer)) != -1) {
						content.write(sBuffer, 0, readBytes);
					}
					String dataAsString = new String(content.toByteArray());
					Log.v(getClass().getCanonicalName(), dataAsString);
					AuthReader ftr = new AuthReader();
					flickrAPIAuthToken = ftr.getAuth(dataAsString).token;
					prefs.edit()
							.putString(keyFlickrAPIAuthToken,
									flickrAPIAuthToken).commit();
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(FlickrAuthActivity.this,
									R.string.auth_token_saved,
									Toast.LENGTH_LONG).show();
						}
					});
				} catch (IOException e) {
					Log.e(getClass().getCanonicalName(), e.toString());
					Log.e(getClass().getCanonicalName(), e.getMessage());
				}
			}

		}).start();
	}
}