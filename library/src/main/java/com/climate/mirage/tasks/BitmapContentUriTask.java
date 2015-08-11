package com.climate.mirage.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BitmapContentUriTask extends BitmapTask {

	private MirageRequest request;
	private Context context;

	public BitmapContentUriTask(Context context, Mirage mirage, MirageRequest request,
								LoadErrorManager loadErrorManager,
								Callback<Bitmap> callback) {
		super(mirage, request, loadErrorManager, callback);
		this.request = request;
		this.context = context;
	}

	@Override
	protected Bitmap loadFromExternal() throws IOException {
		Bitmap bitmap = null;
		InputStream in = context.getContentResolver().openInputStream(request.uri());

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

	private boolean isSaveSource() {
		return request.isRequestShouldSaveSource();
	}

}