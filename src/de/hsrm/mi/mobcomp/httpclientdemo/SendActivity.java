package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.ProgressMultipartEntity;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.ProgressMultipartEntity.ProgressListener;
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

	private static final int CAPTURE_PICTURE = 2;
	private ImageButton cameraButton;
	private ImageView previewUpload;
	private Bitmap bitmap;
	private Uri selectedImageUri;
	private File imageFile;
	private long totalSize;
	private ProgressDialog pd;

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

		previewUpload = (ImageView) findViewById(R.id.previewUpload);

		cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				File cacheDir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/de.hsrm.mi.mobcomp.httpclientdemo/captures/");
				if (!cacheDir.exists()) {
					if (!cacheDir.mkdirs())
						return;
				}
				// Pfad zur Cache-Datei
				imageFile = new File(cacheDir.getAbsolutePath()
						+ "/"
						+ new SimpleDateFormat("yyyyMMdd_HHmmss")
								.format(new Date()) + ".jpg");

				selectedImageUri = Uri.fromFile(imageFile);

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);

				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						CAPTURE_PICTURE);
			}
		});

		Button uploadButton = (Button) findViewById(R.id.uploadButton);
		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								pd.show();
							}
						});
						uploadBitmap(bitmap);
						handler.post(new Runnable() {
							@Override
							public void run() {
								pd.dismiss();
								Toast.makeText(SendActivity.this,
										"Upload complete", Toast.LENGTH_LONG)
										.show();
							}
						});
					}

				}).start();
			}
		});

		flickrAPI = new RestAPI(flickrAPIKey, flickrAPISecret,
				flickrAPIAuthToken);
		
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Uploading Picture...");
		pd.setCancelable(false);
	}

	/**
	 * @todo TODO: http://www.flickr.com/services/api/upload.example.html
	 * @todo TODO:
	 *       http://blog.tacticalnuclearstrike.com/2010/01/using-multipartentity
	 *       -in-android-applications/
	 * 
	 * @param bitmap
	 * @return
	 */
	private void uploadBitmap(Bitmap bitmap) {
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", flickrAPI.getApiKey());
		params.put("auth_token", flickrAPI.getAuthToken());
		params.put("tags", "httpclientdemo hsrm mobile android");
		String requestSignature = flickrAPI.sign(params);

		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(flickrAPI.getUploadUri().toString());
		
		// MultipartEntity multipartContent = new MultipartEntity();
		ProgressMultipartEntity multipartContent = new ProgressMultipartEntity(new ProgressListener()
		{
			@Override
			public void transferred(long num)
			{
				publishProgress((int) ((num / (float) totalSize) * 100));
			}
		});

		try {
			for (String param : params.keySet()) {
				multipartContent.addPart(param,
						new StringBody(params.get(param)));
			}
			multipartContent.addPart("photo", new FileBody(imageFile));
			multipartContent.addPart("api_sig",
					new StringBody(requestSignature));
			totalSize = multipartContent.getContentLength();
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getCanonicalName(), e.toString());
		}

		request.setEntity(multipartContent);

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

	/**
	 * Progressbar aktualisieren
	 * 
	 * @param i Progress in Prozent 0 > i > 100
	 */
	protected void publishProgress(final int i) {
		Log.v(getClass().getCanonicalName(), "Progress: " + i);
		handler.post(new Runnable() {
			@Override
			public void run() {
				pd.setProgress(i);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == CAPTURE_PICTURE) {
				ContentResolver cr = getContentResolver();
				Bitmap bitmap;
				try {
					bitmap = android.provider.MediaStore.Images.Media
							.getBitmap(cr, selectedImageUri);
					previewUpload.setImageBitmap(bitmap);
				} catch (Exception e) {
					Log.e(getClass().getCanonicalName(), e.toString());
				}
			}
		}
	}
}