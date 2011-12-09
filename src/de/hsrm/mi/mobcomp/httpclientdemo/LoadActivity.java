package de.hsrm.mi.mobcomp.httpclientdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.IdFile;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.ParameterRunnable;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.ImageLoader;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.RestAPI;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.SetsReader;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.SetsReader.Set;

/**
 * Demonstriert, wie man mit dem {@link HttpClient} Daten laden kann.
 * 
 * Verwendet dazu die flickr-API.
 * 
 * @see http://www.flickr.com/services/api/
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class LoadActivity extends MenuActivity {
	private TextView loadingStatus;
	private Handler handler = new Handler();
	private String flickrAPIKey;
	private String flickrNSID;
	private ExecutorService pool;
	private RestAPI flickrAPI;
	private int threadPoolSize;
	private int numSets;
	private HashMap<String, ImageView> id2iv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load);

		// OAuth wird erstmal nicht verwendet, da das nicht zum Thema gehört
		// daher verwenden wir den API-Key und User-ID
		// Eine eigenen API_key erhält man unter:
		// http://www.flickr.com/services/api/keys/
		// Die NSID lässt sich z.B. mittels http://www.xflickr.com/fusr/
		// ermitteln
		SharedPreferences prefs = PrefsActivity.getPreferences(this);
		String keyFlickrAPIKey = getResources().getString(R.string.key_api_key);
		String keyFlickrNSID = getResources().getString(R.string.key_nsid);
		String keyThreadPoolSize = getResources().getString(
				R.string.key_threadpool_size);
		String keyNumSets = getResources().getString(R.string.key_num_sets);
		flickrAPIKey = prefs.getString(keyFlickrAPIKey, null);
		flickrNSID = prefs.getString(keyFlickrNSID, null);
		threadPoolSize = Integer.parseInt(prefs.getString(keyThreadPoolSize,
				"4"));
		numSets = Integer.parseInt(prefs.getString(keyNumSets, "20"));

		// Keine Einstellung vorhanden? Dann Konfigurieren.
		if (flickrAPIKey == null || flickrAPIKey.length() <= 0) {
			startActivity(new Intent(getApplicationContext(),
					PrefsActivity.class));
			Toast.makeText(getApplicationContext(),
					R.string.please_configure_api_key, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		loadingStatus = (TextView) findViewById(R.id.loadingStatus);

		// Thread-Pool, der zum Laden von Fotos verwendet wird
		pool = Executors.newFixedThreadPool(threadPoolSize,
				Executors.defaultThreadFactory());

		// Erzeugt die URLs der flickr-API
		flickrAPI = new RestAPI(flickrAPIKey);

		// Liste der Sets laden
		loadSets();
	}

	/**
	 * Aktualisiert die Statusanzeige unter dem Spinner
	 * 
	 * @param status
	 */
	private void setStatus(final String status) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				loadingStatus.setText(status);
			}
		});
	}

	/**
	 * Lädt die Sets
	 */
	private void loadSets() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setStatus("Loading sets from from flickr ...");
				Uri setsUri = flickrAPI.getPhotoSets(flickrNSID, 1, numSets);

				// HTTP-Client erzeugen
				HttpClient client = new DefaultHttpClient();

				// Wir machen einen GET-Request
				HttpGet request = new HttpGet(setsUri.toString());
				// mit request.setHeader(header, value) können beliebige Header
				// im Request gesetzt werden

				try {
					HttpResponse response = client.execute(request);

					// Http-Status auslesen
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() != 200) {
						throw new IOException("Invalid response from server: "
								+ status.toString());
					}

					// Antwort auslesen und in String speichern
					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();
					ByteArrayOutputStream content = new ByteArrayOutputStream();
					int readBytes = 0;
					byte[] sBuffer = new byte[512];
					while ((readBytes = inputStream.read(sBuffer)) != -1) {
						content.write(sBuffer, 0, readBytes);
					}
					String dataAsString = new String(content.toByteArray());

					// In Datei speichern?
					// Siehe {@link ImageLoader#fetchImage(Uri source)}.

					setStatus("Sets loaded.");

					// Ergebnis auswerten
					setStatus("Parsing XML.");
					ArrayList<Set> sets = new SetsReader()
							.getSets(dataAsString);
					setStatus("XML parsed. " + sets.size() + " Sets loaded.");
					// Jetzt wieder zurück an den UI-Thread um die Sets an zu
					// zeigen
					handler.post(new ParameterRunnable<ArrayList<Set>>(sets) {
						@Override
						public void run() {
							showSets(getParameter());
						}

					});
				} catch (IOException e) {
					// Ein Problem in der HTTP-Kommunikation ist aufgetreten
					handler.post(new ParameterRunnable<String>(e.getMessage()) {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									getParameter(), Toast.LENGTH_LONG).show();
						}
					});
					Log.e(getClass().getCanonicalName(), e.getMessage());
				}
			}

		}).start();
	}

	/**
	 * Zeigt eine Liste mit Sets an
	 * 
	 * @param sets
	 */
	private void showSets(ArrayList<Set> sets) {
		// Layout ersetzen
		setContentView(R.layout.sets);
		LayoutInflater li = getLayoutInflater();
		LinearLayout setsListView = (LinearLayout) findViewById(R.id.sets);
		// Hier merken wir uns die flickr-ID und die ImageView, um später das
		// geladene Bild in der richtigen ImageView setzen zu können.
		id2iv = new HashMap<String, ImageView>();
		for (Set set : sets) {
			LinearLayout setItem = (LinearLayout) li.inflate(
					R.layout.sets_item, setsListView, false);
			TextView setItemText = (TextView) setItem
					.findViewById(R.id.steItemTextView);
			setItemText.setText(set.title);
			setsListView.addView(setItem);
			// ImageView merken
			id2iv.put(set.primaryPhotoId,
					(ImageView) setItem.findViewById(R.id.steItemImageView));
			// Jetzt die Bilder über den ThreadPool laden

			// Mit {@link IdFile} schleifen wir die ID des Sets durch alle
			// Threads durch
			IdFile idFile = new IdFile(set.primaryPhotoId);
			ParameterRunnable<IdFile> onComplete = new ParameterRunnable<IdFile>(
					idFile) {
				@Override
				public void run() {
					// Im IdFile ist jetzt die Datei des Thumbnails gesetzt
					handler.post(new ParameterRunnable<IdFile>(getParameter()) {
						@Override
						public void run() {
							// Image-View wieder finden ...
							ImageView setItemImage = id2iv.get(getParameter()
									.getId());
							// Und Bild aus Datei setzen
							setItemImage.setImageBitmap(BitmapFactory
									.decodeFile(getParameter().getFile()
											.getAbsolutePath()));
						}
					});
				}
			};
			// Das Laden der Bilder geschieht im ThreadPool, damit wir nicht zu
			// viele Connections gleichzeitig haben
			ImageLoader il = new ImageLoader(flickrAPI, set.primaryPhotoId,
					onComplete);
			pool.execute(il);
		}
	}
}