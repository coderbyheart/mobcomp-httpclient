package de.hsrm.mi.mobcomp.httpclientdemo.flickr;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.ParameterRunnable;
import de.hsrm.mi.mobcomp.httpclientdemo.extra.IdFile;
import de.hsrm.mi.mobcomp.httpclientdemo.flickr.SizesReader.Size;

/**
 * Lädet Bilder via der {@link RestAPI flickr-API} herunter und speichert diese
 * auf der SD-Karte
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class ImageLoader implements Runnable {

	private RestAPI flickr;
	private String id;
	private ParameterRunnable<IdFile> onComplete;

	public ImageLoader(RestAPI flickr, String id,
			ParameterRunnable<IdFile> extraRunnable) {
		this.flickr = flickr;
		this.id = id;
		this.onComplete = extraRunnable;
	}

	/**
	 * Lädt die Infos zu den verschiedenen Größen eines flickr-Photos herunter und speichert das Quadratische Thumbnail.
	 * Anschließend wird das onComplete-Runnable ausgeführt
	 */
	@Override
	public void run() {
		Uri sizesUri = flickr.getPhotoSizes(id);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(sizesUri.toString());
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

			SizesReader sr = new SizesReader(id);
			ArrayList<Size> sizes = sr.getSizes(dataAsString);
			for (Size s : sizes) {
				if (s.type.equals(RestAPI.SIZE_SQUARE)) {
					onComplete.getParameter().setFile(fetchImage(s.source));
					onComplete.run();
				}
			}
		} catch (IOException e) {
			Log.e(getClass().getCanonicalName(), e.toString());
			Log.e(getClass().getCanonicalName(), e.getMessage());
		}
	}

	/**
	 * @todo Not covered here: Check if external storage is available, see
	 *       http://developer.android.com/guide/topics/data/data-storage.html#
	 *       filesExternal
	 * @param source
	 * @throws IOException
	 */
	public File fetchImage(Uri source) throws IOException {
		// Note: In API Level < 11 muss man sich das Verzeichnis noch händisch
		// zusammensetzen
		File cacheDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/de.hsrm.mi.mobcomp.httpclientdemo/cache/");
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs())
				throw new IOException("Failed to create: "
						+ cacheDir.getAbsolutePath());
		}
		// Pfad zur Cache-Datei
		File imageFile = new File(cacheDir.getAbsolutePath() + "/" + id
				+ ".thumb");
		// Note: Hier könnte man prüfen, ob es die Cache-Datei schon gibt, zum
		// Demonstrationszwecken wird dies aber explizit nicht gemacht
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(source.toString());
		HttpResponse response = client.execute(request);
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != 200) {
			throw new IOException("Invalid response from server: "
					+ status.toString());
		}
		HttpEntity entity = response.getEntity();
		InputStream inputStream = entity.getContent();
		// Datei zum Schreiben öffnen
		BufferedOutputStream buf = new BufferedOutputStream(
				new FileOutputStream(imageFile));

		int readBytes = 0;
		byte[] sBuffer = new byte[512];
		while ((readBytes = inputStream.read(sBuffer)) != -1) {
			buf.write(sBuffer, 0, readBytes);
		}
		// Datei schließen nicht vergessen
		buf.close();
		return imageFile;

	}
}
