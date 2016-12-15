package com.climate.mirage.tasks;

import android.content.Context;
import android.graphics.Bitmap;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;

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
	protected InputStream createInputStreamForExternal() throws IOException {
		InputStream in = context.getContentResolver().openInputStream(request.uri());
		return in;
	}

}