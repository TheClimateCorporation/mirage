package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.exceptions.MirageOomException;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class BitmapUrlTask extends BitmapTask {

	private MirageRequest request;
	private static final int IO_BUFFER_SIZE = 8 * 1024;

	public BitmapUrlTask(Mirage mirage, MirageRequest request,
						 LoadErrorManager loadErrorManager,
						 Callback<Bitmap> callback) {
		super(mirage, request, loadErrorManager, callback);
		this.request = request;
	}


	protected Bitmap loadFromExternal() throws IOException {
		return doMirageUrlLoad(true);
	}

	private Bitmap doMirageUrlLoad(boolean retryOnOutOfMemory) throws java.io.IOException {
		Bitmap bitmap = null;
		try {
			bitmap = loadExternalBitmap();
		} catch (OutOfMemoryError e) {
			if (request.memoryCache() != null) request.memoryCache().clear();
			System.gc();
			if (retryOnOutOfMemory) {
				bitmap = doMirageUrlLoad(false);
			} else {
				throw new MirageOomException(Mirage.Source.EXTERNAL);
			}
		}

		return bitmap;
	}

	private Bitmap loadExternalBitmap() throws IOException {
		Bitmap bitmap = null;
		URLConnection connection = getConnection();
		InputStream in = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);

		// if we need to keep the source, stream it directly to a file
		// we can't load it to memory first and then write to file because
		// they could be bitmap options on the stream.
		if (isSaveSource()) {
			request.diskCache().put(request.getSourceKey(), new InputStreamWriter(in));
			IOUtils.close(in);
			File file = request.diskCache().get(request.getSourceKey());
			if (file != null) {
				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), request.options());
			}
		} else {
			bitmap = BitmapFactory.decodeStream(in, request.outPadding(), request.options());
			IOUtils.close(in);
		}

		return bitmap;
	}

	private URLConnection getConnection() throws IOException {
		return request.urlFactory().getConnection(request.uri());
	}

	private boolean isSaveSource() {
		return request.isRequestShouldSaveSource();
	}

}