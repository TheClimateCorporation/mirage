package com.climate.mirage.cache.disk;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.OutputStream;

public class BitmapEncoder implements Encoder<Bitmap> {
	private static final String TAG = "BitmapEncoder";
	private static final int DEFAULT_COMPRESSION_QUALITY = 90;
	private Bitmap.CompressFormat compressFormat;
	private int quality;

	public BitmapEncoder() {
		this(null, DEFAULT_COMPRESSION_QUALITY);
	}

	public BitmapEncoder(@Nullable Bitmap.CompressFormat compressFormat, int quality) {
		this.compressFormat = compressFormat;
		this.quality = quality;
	}

	public boolean encode(Bitmap bitmap, OutputStream os) {
		Bitmap.CompressFormat format = getFormat(bitmap);
		bitmap.compress(format, quality, os);
		return true;
	}

	private Bitmap.CompressFormat getFormat(Bitmap bitmap) {
		if (compressFormat != null) {
			return compressFormat;
		} else if (bitmap.hasAlpha()) {
			return Bitmap.CompressFormat.PNG;
		} else {
			return Bitmap.CompressFormat.JPEG;
		}
	}

}
