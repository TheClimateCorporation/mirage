package com.climate.mirage.tasks;

import android.graphics.Bitmap;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads a bitmap from a file on the device
 */
public class BitmapFileTask extends BitmapTask {

	private MirageRequest request;

	public BitmapFileTask(Mirage mirage, MirageRequest request,
					  LoadErrorManager loadErrorManager,
					  Callback<Bitmap> callback) {
		super(mirage, request, loadErrorManager, callback);
		this.request = request;
	}

	@Override
	protected InputStream createInputStreamForExternal() throws IOException {
        File file = new File(request.uri().getPath());
        InputStream in = new FileInputStream(file);
		return in;
	}

}