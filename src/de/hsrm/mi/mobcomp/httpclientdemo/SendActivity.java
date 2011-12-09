package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.RestAPI;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.UploadReader;

/**
 * Demonstriert, wie man mit dem {@link HttpClient} Daten laden kann.
 * 
 * Verwendet dazu die flickr-API.
 * 
 * @see http://www.flickr.com/services/api/
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class SendActivity extends MenuActivity {

	private String flickrAPIKey;
	private RestAPI flickrAPI;
	private String flickrAPISecret;
	private Handler handler = new Handler();
	private String flickrAPIAuthToken;

	private static final int SELECT_PICTURE = 1;
	private ImageButton imageButton;
	private Uri selectedImageUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PrefsActivity.getPreferences(this);
		String keyFlickrAPIKey = getResources().getString(R.string.key_api_key);
		String keyFlickrAPISecret = getResources().getString(
				R.string.key_api_secret);
		String keyFlickrAPIAuthToken = getResources().getString(
				R.string.key_api_auth_token);
		flickrAPIKey = prefs.getString(keyFlickrAPIKey, null);
		flickrAPISecret = prefs.getString(keyFlickrAPISecret, null);
		flickrAPIAuthToken = prefs.getString(keyFlickrAPIAuthToken, null);

		// Keine Einstellung vorhanden? Dann Konfigurieren.
		if (flickrAPIAuthToken == null || flickrAPIAuthToken.length() <= 0) {
			startActivity(new Intent(getApplicationContext(),
					FlickrAuthActivity.class));
			finish();
			return;
		}

		setContentView(R.layout.send);

		imageButton = (ImageButton) findViewById(R.id.uploadImageButton);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);
			}
		});

		Button uploadButton = (Button) findViewById(R.id.uploadButton);
		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Bitmap bitmap = getBitmapFromUri();
						uploadBitmap(bitmap);
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(SendActivity.this, "Upload complete", Toast.LENGTH_LONG).show();
							}
						});
					}

				}).start();
			}
		});
		
		flickrAPI = new RestAPI(flickrAPIKey, flickrAPISecret, flickrAPIAuthToken);
	}

	private Bitmap getBitmapFromUri() {
		try {
			// Um and die Daten einer Bild-URL
			// (content://media/external/images/media/3)
			// zu
			// kommen, verwenden wir den MediaStore
			return MediaStore.Images.Media.getBitmap(getContentResolver(),
					selectedImageUri);
		} catch (FileNotFoundException e) {
			Log.e(getClass().getCanonicalName(), e.toString());
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.toString());

		}
		return null;
	}

	private void uploadBitmap(Bitmap bitmap) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = flickrAPI.getUploadRequest(bitmap);

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

			UploadReader ur = new UploadReader();
			String photoId = ur.getPhotoId(dataAsString);
			Log.v(getClass().getCanonicalName(), photoId);
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.toString());
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				selectedImageUri = data.getData();
				Log.v(getClass().getCanonicalName(),
						selectedImageUri.toString());
				imageButton.setImageURI(selectedImageUri);
			}
		}
	}
}