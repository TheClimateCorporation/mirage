package com.climate.mirage.cache.memory;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapLruCache extends LruCacheAdapter<String, Bitmap> {

	private LruCache<String, Bitmap> impl;
	
	public BitmapLruCache(float percentOfAvailableMemory) {
		super(new MyCache((int)(Runtime.getRuntime().maxMemory() * percentOfAvailableMemory)));
		impl = getLruCache();
	}
	
	public BitmapLruCache(int maxSize) {
		super(new MyCache(maxSize));
		impl = getLruCache();
	}

	@Override
	public boolean has(String key) {
		if (key == null) return false;
		Bitmap value = get(key);
		return (value != null && !value.isRecycled());
	}
	
	private static class MyCache extends LruCache<String, Bitmap> {
		public MyCache(int maxSize) {
			super(maxSize);
		}
		
		@Override
		protected int sizeOf(String key, Bitmap value) {
			if (value == null) return 0;
			return getBitmapSize(value);
		}
		
		/**
	     * Get the size in bytes of a bitmap.
	     * @param bitmap
	     * @return size in bytes
	     */
	    private int getBitmapSize(Bitmap bitmap) {
			return bitmap.getByteCount();
	    }
	}
	
}