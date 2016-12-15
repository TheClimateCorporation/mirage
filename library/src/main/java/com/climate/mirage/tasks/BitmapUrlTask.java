package com.climate.mirage.tasks;

import android.graphics.Bitmap;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;

import java.io.BufferedInputStream;
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

	@Override
	protected InputStream createInputStreamForExternal() throws IOException {
		URLConnection connection = getConnection();
		InputStream in = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
		return in;
	}

    private URLConnection getConnection() throws IOException {
		return request.urlFactory().getConnection(request.uri());
	}

}