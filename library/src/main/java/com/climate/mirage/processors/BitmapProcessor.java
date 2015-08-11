package com.climate.mirage.processors;

import android.graphics.Bitmap;

public interface BitmapProcessor {
	public String getId();
	public Bitmap process(Bitmap in);
}