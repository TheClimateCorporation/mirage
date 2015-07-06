package com.climate.mirage.cache.disk.writers;

import android.graphics.Bitmap;
import android.util.Log;

import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapWriter implements DiskCache.Writer {
	private Bitmap bitmap;
	private static final String TAG = BitmapWriter.class.getSimpleName();

	public BitmapWriter(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public boolean write(File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (bitmap.hasAlpha()) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			} else {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
			}
			return true;
		} catch (IOException e) {
			Log.w(TAG, "Could not write bitmap to file", e);
		} finally {
			IOUtils.close(fos);
		}
		return false;
	}
}