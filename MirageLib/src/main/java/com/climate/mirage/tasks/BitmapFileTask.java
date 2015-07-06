package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;

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
	protected Bitmap loadFromExternal() throws IOException {
		Bitmap bitmap = null;
		File file = new File(request.uri().getPath());
		InputStream in = new FileInputStream(file);

		if (isSaveSource()) {
			request.diskCache().put(request.getSourceKey(), new InputStreamWriter(in));
			IOUtils.close(in);
			File savedFile = request.diskCache().get(request.getSourceKey());
			if (savedFile != null) {
				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), request.options());
			}
		} else {
			bitmap = BitmapFactory.decodeStream(in, request.outPadding(), request.options());
			IOUtils.close(in);
		}

		return bitmap;

	}

	private boolean isSaveSource() {
		return request.isRequestShouldSaveSource();
	}

}