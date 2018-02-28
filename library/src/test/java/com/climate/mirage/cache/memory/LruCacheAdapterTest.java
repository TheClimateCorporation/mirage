package com.climate.mirage.cache.memory;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class LruCacheAdapterTest extends RobolectricTest {

	private static final int DEFAULT_SIZE = 15 * 1024 * 1024;

	@Test
	public void testSimpleAdd() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
	}

	@Test
	public void testSimpleOnNullKey() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		Assert.assertFalse(cache.has(null));
	}

	@Test
	public void testCacheIsEmpty() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		Assert.assertFalse(cache.has("asd"));
		Assert.assertEquals(0, cache.getCurrentSize());
	}

	@Test
	public void testCacheDeletes() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		cache.remove("asd");
		Assert.assertFalse(cache.has("asd"));
	}

	@Test
	public void testClear() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		cache.clear();
		Assert.assertFalse(cache.has("asd"));
	}

	@Test
	public void testGet() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		Assert.assertNotNull(cache.get("asd"));
	}

	@Test
	public void testPutNull() throws IOException {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		cache.put(null, null);
		Assert.assertTrue("This should run gracefully", true);
	}

	@Test
	public void testMaxSize() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		Assert.assertEquals(DEFAULT_SIZE, cache.getMaxSize());
	}

	@Test
	public void testCurrentSize() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		cache.put("asd", bitmap);
		Assert.assertTrue(cache.getCurrentSize() > 0);
		Assert.assertEquals(bitmap.getByteCount(), cache.getCurrentSize());
	}

	@Test
	public void testDelegateNotNull() {
		LruCacheAdapter<String, Bitmap> cache =
				new LruCacheAdapter<String, Bitmap>(new MyCache(DEFAULT_SIZE));
		Assert.assertNotNull(cache.getLruCache());
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
		@SuppressLint("NewApi")
		private int getBitmapSize(Bitmap bitmap) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
				return bitmap.getByteCount();
			}
			// Pre HC-MR1
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}

}