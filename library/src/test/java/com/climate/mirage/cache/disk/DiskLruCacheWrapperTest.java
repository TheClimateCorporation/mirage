package com.climate.mirage.cache.disk;

import android.content.Context;
import android.graphics.Bitmap;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;
import com.climate.mirage.cache.disk.writers.BitmapWriter;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class DiskLruCacheWrapperTest {

	@Before
	public void setUp() throws Exception {
		File file = new File(getContext().getCacheDir(), "blank");
		file.delete();
		File file2 = new File(getContext().getCacheDir(), "fooey");
		file2.delete();
		getDefaultWrapper().clear();
	}

	@After
	public void tearDown() throws Exception {
		File file = new File(getContext().getCacheDir(), "blank");
		file.delete();
		File file2 = new File(getContext().getCacheDir(), "fooey");
		file2.delete();
		getDefaultWrapper().clear();
	}

	@Test
	public void testSharedDiskLruCacheFactorySameInstance() throws Exception {
		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper1 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024);

		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper2 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024);

		Assert.assertSame(wrapper1.getDiskCache(), wrapper2.getDiskCache());
	}

	@Test
	public void testNotSharedDiskLruCacheFactorySameInstance() throws Exception {
		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper1 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "fooey"),
						100 * 1024 * 1024);

		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper2 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024);

		Assert.assertNotSame(wrapper1.getDiskCache(), wrapper2.getDiskCache());
	}

	@Test
	public void testSharedDiskLruCacheFactoryReset() throws Exception {
		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper1 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024);

		DiskLruCacheWrapper.SharedDiskLruCacheFactory wrapper2 =
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024);
		wrapper1.resetDiskCache();
	}

	@Test
	public void testWrapperDoesntAdd() throws IOException {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024));
		cacheDisk.setReadOnly(true);
		cacheDisk.put("fooey", new BitmapWriter(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));

		Assert.assertNull(cacheDisk.get("fooey"));
	}

	@Test
	public void testWrapperDoesAdd() throws IOException {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024));
		cacheDisk.put("fooey", new BitmapWriter(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));

		Assert.assertNotNull(cacheDisk.get("fooey"));
	}

	@Test
	public void testWrapperDeletes() throws IOException {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024));
		cacheDisk.put("fooey", new BitmapWriter(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));
		Assert.assertNotNull(cacheDisk.get("fooey"));
		cacheDisk.delete("fooey");
		Assert.assertNull(cacheDisk.get("fooey"));
	}

	@Test
	public void testWrapperClears() throws IOException {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024));
		cacheDisk.put("fooey", new BitmapWriter(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));
		Assert.assertNotNull(cacheDisk.get("fooey"));
		cacheDisk.clear();
		Assert.assertNull(cacheDisk.get("fooey"));
	}

	@Test
	public void testIOsExceptionGraceful() throws IOException {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(new DiskLruCacheWrapper.DiskLruCacheFactory() {
			@Override
			public DiskLruCache getDiskCache() throws IOException {
				throw new IOException("exception for test");
			}

			@Override
			public void resetDiskCache() {

			}
		});

		Assert.assertNull(cacheDisk.get("fooey"));
		cacheDisk.put("fooey", new BitmapWriter(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));
		cacheDisk.delete("fooey");
		cacheDisk.clear();
		// if the test gets to this point it all worked as expected
		// and the IOException was handled gracefully
	}

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	private DiskLruCacheWrapper getDefaultWrapper() {
		DiskLruCacheWrapper cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
						new File(getContext().getCacheDir(), "blank"),
						100 * 1024 * 1024));
		return cacheDisk;
	}

}