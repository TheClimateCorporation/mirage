package com.climate.mirage.utils;

import android.support.annotation.Nullable;

import java.io.Closeable;

public final class IOUtils {

	private IOUtils() {}

	public static void close(@Nullable Closeable stream) {
		try {
			if (stream != null) stream.close();
		} catch (Exception e) {
			// do nothing. code doesn't care.
		}
	}

}
