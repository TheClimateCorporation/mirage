package com.climate.mirage.cache.memory;

import android.graphics.Bitmap;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

public class BitmapLruCacheTest extends RobolectricTest {

	private static final int DEFAULT_SIZE = 15 * 1024 * 1024;

	@Test
	public void testSimpleAdd() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
	}

	@Test
	public void testSimpleOnNullKey() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		Assert.assertFalse(cache.has(null));
	}

	@Test
	public void testCacheIsEmpty() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		Assert.assertFalse(cache.has("asd"));
		Assert.assertEquals(0, cache.getCurrentSize());
	}

	@Test
	public void testCacheDeletes() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		cache.remove("asd");
		Assert.assertFalse(cache.has("asd"));
	}

	@Test
	public void testClear() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		cache.clear();
		Assert.assertFalse(cache.has("asd"));
	}

	@Test
	public void testGet() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		cache.put("asd", Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
		Assert.assertTrue(cache.has("asd"));
		Assert.assertNotNull(cache.get("asd"));
	}

	@Test
	public void testMaxSize() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		Assert.assertEquals(DEFAULT_SIZE, cache.getMaxSize());
	}

	@Test
	public void testCurrentSize() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		Assert.assertEquals(0, cache.getCurrentSize());
		Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
		cache.put("asd", bitmap);
		Assert.assertEquals(bitmap.getByteCount(), cache.getCurrentSize());
		cache.clear();
		Assert.assertEquals(0, cache.getCurrentSize());
	}

	@Test
	public void testDelegateNotNull() {
		BitmapLruCache cache = new BitmapLruCache(DEFAULT_SIZE);
		Assert.assertNotNull(cache.getLruCache());
	}

	@Test
	public void testFloatConstructor() {
		BitmapLruCache cache = new BitmapLruCache(0.25f);
		Assert.assertTrue(cache.getMaxSize() > 0);
	}

}